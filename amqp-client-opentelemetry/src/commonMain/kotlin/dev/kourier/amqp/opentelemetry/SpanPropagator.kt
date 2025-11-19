package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.Field
import dev.kourier.amqp.Properties
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.Context

/**
 * Handles W3C Trace Context propagation via AMQP message headers.
 *
 * Implements the W3C Trace Context specification for distributed tracing:
 * https://www.w3.org/TR/trace-context/
 */
object SpanPropagator {

    private const val TRACE_PARENT_HEADER = "traceparent"
    private const val TRACE_STATE_HEADER = "tracestate"

    /**
     * Injects trace context into message properties headers.
     * Creates or updates the headers map with traceparent and tracestate.
     *
     * @param properties The message properties to inject into.
     * @param context The OpenTelemetry context containing the span to propagate.
     * @return Updated properties with trace context in headers.
     */
    fun inject(properties: Properties, context: Context = Context.current()): Properties {
        val span = Span.fromContext(context)
        val spanContext = span.spanContext

        // Don't inject if span is not valid/sampled
        if (!spanContext.isValid) {
            return properties
        }

        val headers = properties.headers?.toMutableMap() ?: mutableMapOf()

        // W3C Trace Context format: 00-{trace-id}-{span-id}-{trace-flags}
        val traceparent = buildTraceparent(spanContext)
        headers[TRACE_PARENT_HEADER] = Field.LongString(traceparent)

        // Add tracestate if present
        val traceState = spanContext.traceState
        if (!traceState.isEmpty) {
            headers[TRACE_STATE_HEADER] = Field.LongString(traceState.asMap().entries.joinToString(",") { "${it.key}=${it.value}" })
        }

        return properties.copy(headers = headers)
    }

    /**
     * Extracts trace context from message properties headers.
     * Returns parent context or root context if headers are missing/invalid.
     *
     * @param properties The message properties to extract from.
     * @return OpenTelemetry context with extracted span context, or root context if not found.
     */
    fun extract(properties: Properties?): Context {
        val headers = properties?.headers ?: return Context.root()

        val traceparent = (headers[TRACE_PARENT_HEADER] as? Field.LongString)?.value
            ?: return Context.root()

        val tracestate = (headers[TRACE_STATE_HEADER] as? Field.LongString)?.value

        return try {
            val spanContext = parseTraceparent(traceparent, tracestate)
            if (spanContext.isValid) {
                Context.root().with(Span.wrap(spanContext))
            } else {
                Context.root()
            }
        } catch (e: Exception) {
            // Invalid traceparent format, return root context
            Context.root()
        }
    }

    /**
     * Builds a W3C traceparent header value from a span context.
     * Format: 00-{trace-id}-{span-id}-{trace-flags}
     */
    private fun buildTraceparent(spanContext: SpanContext): String {
        return "00-${spanContext.traceId}-${spanContext.spanId}-${formatTraceFlags(spanContext.traceFlags)}"
    }

    /**
     * Parses a W3C traceparent header value into a SpanContext.
     * Format: version-trace-id-parent-id-trace-flags
     */
    private fun parseTraceparent(traceparent: String, tracestate: String?): SpanContext {
        val parts = traceparent.split("-")
        if (parts.size != 4) {
            return SpanContext.getInvalid()
        }

        val version = parts[0]
        if (version != "00") {
            // Unknown version, return invalid
            return SpanContext.getInvalid()
        }

        val traceId = parts[1]
        val spanId = parts[2]
        val traceFlags = parts[3]

        // Validate lengths
        if (traceId.length != 32 || spanId.length != 16 || traceFlags.length != 2) {
            return SpanContext.getInvalid()
        }

        // Parse trace flags
        val traceFlagsValue = try {
            traceFlags.toInt(16).toByte()
        } catch (e: NumberFormatException) {
            return SpanContext.getInvalid()
        }

        // Parse tracestate
        val traceStateObj = parseTracestate(tracestate)

        return SpanContext.createFromRemoteParent(
            traceId,
            spanId,
            TraceFlags.fromByte(traceFlagsValue),
            traceStateObj
        )
    }

    /**
     * Formats trace flags as a 2-digit hex string.
     */
    private fun formatTraceFlags(flags: TraceFlags): String {
        return flags.asByte().toUByte().toString(16).padStart(2, '0')
    }

    /**
     * Parses W3C tracestate header value into TraceState.
     * Format: key1=value1,key2=value2,...
     */
    private fun parseTracestate(tracestate: String?): TraceState {
        if (tracestate.isNullOrBlank()) {
            return TraceState.getDefault()
        }

        var builder = TraceState.builder()
        val entries = tracestate.split(",")

        for (entry in entries) {
            val keyValue = entry.trim().split("=", limit = 2)
            if (keyValue.size == 2) {
                val key = keyValue[0].trim()
                val value = keyValue[1].trim()
                if (key.isNotEmpty() && value.isNotEmpty()) {
                    builder = builder.put(key, value)
                }
            }
        }

        return builder.build()
    }

}
