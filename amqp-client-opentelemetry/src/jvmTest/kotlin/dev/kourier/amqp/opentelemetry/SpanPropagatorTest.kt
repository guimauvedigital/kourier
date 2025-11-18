package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.Field
import dev.kourier.amqp.Properties
import io.opentelemetry.api.trace.*
import io.opentelemetry.context.Context
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SpanPropagatorTest {

    @Test
    fun testInjectTraceparentHeader() {
        // Create a mock span context
        val traceId = "0af7651916cd43dd8448eb211c80319c"
        val spanId = "b7ad6b7169203331"
        val traceFlags = TraceFlags.getSampled()

        val spanContext = SpanContext.createFromRemoteParent(
            traceId,
            spanId,
            traceFlags,
            TraceState.getDefault()
        )

        val span = Span.wrap(spanContext)
        val context = Context.root().with(span)

        // Inject into properties
        val properties = Properties()
        val injected = SpanPropagator.inject(properties, context)

        // Verify traceparent header was added
        assertNotNull(injected.headers)
        val traceparent = (injected.headers!!["traceparent"] as? Field.LongString)?.value
        assertNotNull(traceparent)

        // Verify format: 00-{trace-id}-{span-id}-{flags}
        val parts = traceparent.split("-")
        assertEquals(4, parts.size)
        assertEquals("00", parts[0]) // version
        assertEquals(traceId, parts[1])
        assertEquals(spanId, parts[2])
        assertEquals("01", parts[3]) // sampled flag
    }

    @Test
    fun testInjectWithExistingHeaders() {
        // Create span context
        val spanContext = SpanContext.createFromRemoteParent(
            "0af7651916cd43dd8448eb211c80319c",
            "b7ad6b7169203331",
            TraceFlags.getSampled(),
            TraceState.getDefault()
        )
        val span = Span.wrap(spanContext)
        val context = Context.root().with(span)

        // Properties with existing headers
        val existingHeaders = mapOf(
            "custom-header" to Field.LongString("custom-value")
        )
        val properties = Properties(headers = existingHeaders)

        // Inject trace context
        val injected = SpanPropagator.inject(properties, context)

        // Verify both headers exist
        assertNotNull(injected.headers)
        assertNotNull(injected.headers!!["traceparent"])
        assertNotNull(injected.headers!!["custom-header"])
    }

    @Test
    fun testExtractTraceparentHeader() {
        // Create properties with traceparent header
        val traceparent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"
        val headers = mapOf(
            "traceparent" to Field.LongString(traceparent)
        )
        val properties = Properties(headers = headers)

        // Extract context
        val context = SpanPropagator.extract(properties)

        // Verify span context was extracted
        val span = Span.fromContext(context)
        val spanContext = span.spanContext
        assertTrue(spanContext.isValid)
        assertEquals("0af7651916cd43dd8448eb211c80319c", spanContext.traceId)
        assertEquals("b7ad6b7169203331", spanContext.spanId)
        assertTrue(spanContext.isSampled)
    }

    @Test
    fun testExtractWithTracestate() {
        // Create properties with both traceparent and tracestate
        val traceparent = "00-0af7651916cd43dd8448eb211c80319c-b7ad6b7169203331-01"
        val tracestate = "vendor1=value1,vendor2=value2"
        val headers = mapOf(
            "traceparent" to Field.LongString(traceparent),
            "tracestate" to Field.LongString(tracestate)
        )
        val properties = Properties(headers = headers)

        // Extract context
        val context = SpanPropagator.extract(properties)

        // Verify span context
        val span = Span.fromContext(context)
        val spanContext = span.spanContext
        assertTrue(spanContext.isValid)

        // Verify tracestate
        val extractedTraceState = spanContext.traceState
        assertEquals("value1", extractedTraceState.get("vendor1"))
        assertEquals("value2", extractedTraceState.get("vendor2"))
    }

    @Test
    fun testExtractWithMissingHeaders() {
        // Properties without trace headers
        val properties = Properties()

        // Extract should return root context
        val context = SpanPropagator.extract(properties)
        val span = Span.fromContext(context)
        val spanContext = span.spanContext

        // Should be invalid/root context
        assertTrue(!spanContext.isValid)
    }

    @Test
    fun testExtractWithInvalidTraceparent() {
        // Invalid traceparent format
        val headers = mapOf(
            "traceparent" to Field.LongString("invalid-format")
        )
        val properties = Properties(headers = headers)

        // Extract should return root context
        val context = SpanPropagator.extract(properties)
        val span = Span.fromContext(context)
        val spanContext = span.spanContext

        // Should be invalid
        assertTrue(!spanContext.isValid)
    }

    @Test
    fun testRoundTrip() {
        // Create original span context
        val originalTraceId = "0af7651916cd43dd8448eb211c80319c"
        val originalSpanId = "b7ad6b7169203331"
        val originalSpanContext = SpanContext.createFromRemoteParent(
            originalTraceId,
            originalSpanId,
            TraceFlags.getSampled(),
            TraceState.builder().put("vendor", "value").build()
        )
        val originalSpan = Span.wrap(originalSpanContext)
        val originalContext = Context.root().with(originalSpan)

        // Inject
        val properties = Properties()
        val injected = SpanPropagator.inject(properties, originalContext)

        // Extract
        val extractedContext = SpanPropagator.extract(injected)
        val extractedSpan = Span.fromContext(extractedContext)
        val extractedSpanContext = extractedSpan.spanContext

        // Verify round-trip preservation
        assertEquals(originalTraceId, extractedSpanContext.traceId)
        assertEquals(originalSpanId, extractedSpanContext.spanId)
        assertTrue(extractedSpanContext.isSampled)
        assertEquals("value", extractedSpanContext.traceState.get("vendor"))
    }

}
