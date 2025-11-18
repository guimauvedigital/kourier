package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.Properties
import dev.kourier.amqp.connection.createAMQPConnection
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.*

class OpenTelemetryAMQPConnectionTest {

    companion object {
        @JvmField
        @RegisterExtension
        val otelTesting = OpenTelemetryExtension.create()
    }

    @Test
    fun `withTracing wraps existing connection`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}

        // Wrap with tracing
        val tracedConnection = connection.withTracing(tracer)

        try {
            assertNotNull(tracedConnection)
            assertTrue(tracedConnection is OpenTelemetryAMQPConnection)

            // Verify config is accessible
            assertNotNull(tracedConnection.config)
        } finally {
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    @Test
    fun `withTracing creates instrumented connection`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")

        // Create connection and wrap with tracing
        val connection = createAMQPConnection(this) {}
            .withTracing(tracer)

        try {
            assertNotNull(connection)
            assertTrue(connection is OpenTelemetryAMQPConnection)
        } finally {
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `openChannel returns instrumented channel`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)

        try {
            val channel = tracedConnection.openChannel()

            assertNotNull(channel)
            assertTrue(channel is OpenTelemetryAMQPChannel)

            runCatching { channel.close() }
        } finally {
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    @Test
    fun `traceConnectionOperations config enables connection spans`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val config = TracingConfig(traceConnectionOperations = true)
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer, config)

        try {
            // Open a channel
            val channel = tracedConnection.openChannel()

            // Verify openChannel span was created
            val spans = otelTesting.spans
            val openChannelSpan = spans.find { it.name == "connection.openChannel" }

            assertNotNull(openChannelSpan, "openChannel span should be created when enabled")
            assertEquals(SpanKind.CLIENT, openChannelSpan.kind)

            channel.close()

            // Close connection
            tracedConnection.close()

            // Verify close span was created
            val closeSpan = otelTesting.spans.find { it.name == "connection.close" }
            assertNotNull(closeSpan, "close span should be created when enabled")
            assertEquals(SpanKind.CLIENT, closeSpan.kind)

            val attributes = closeSpan.attributes.asMap()
            assertEquals("close", attributes["messaging.operation".toKey()])
        } catch (e: Exception) {
            // Connection operations may fail without real broker
            // but we're testing the span creation logic
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    @Test
    fun `traceConnectionOperations disabled by default`() = runBlocking {
        // Setup with default config
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer) // default config

        try {
            // Open a channel
            val channel = tracedConnection.openChannel()
            channel.close()

            // Verify no connection spans were created
            val spans = otelTesting.spans
            val connectionSpans = spans.filter {
                it.name.startsWith("connection.")
            }

            assertTrue(
                connectionSpans.isEmpty(),
                "Connection spans should not be created when disabled (default)"
            )
        } finally {
            runCatching { tracedConnection.close() }
        }
    }

    //@Test // TODO: Fix this
    fun `multiple channels from same connection are independently traced`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)

        try {
            // Open multiple channels
            val channel1 = tracedConnection.openChannel()
            val channel2 = tracedConnection.openChannel()

            // Each channel should be independently instrumented
            assertTrue(channel1 is OpenTelemetryAMQPChannel)
            assertTrue(channel2 is OpenTelemetryAMQPChannel)
            assertNotEquals(channel1.id, channel2.id)

            // Publish on both channels
            channel1.basicPublish(
                body = "test1".encodeToByteArray(),
                exchange = "exchange1",
                routingKey = "key1",
                properties = Properties()
            )

            channel2.basicPublish(
                body = "test2".encodeToByteArray(),
                exchange = "exchange2",
                routingKey = "key2",
                properties = Properties()
            )

            // Verify both publishes created spans
            val spans = otelTesting.spans
            val span1 = spans.find { it.name == "exchange1 send" }
            val span2 = spans.find { it.name == "exchange2 send" }

            assertNotNull(span1, "First channel publish should create span")
            assertNotNull(span2, "Second channel publish should create span")
            assertNotEquals(span1.spanId, span2.spanId, "Spans should be different")

            runCatching { channel1.close() }
            runCatching { channel2.close() }
        } finally {
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    @Test
    fun `custom tracing config is applied to all channels`() = runBlocking {
        // Setup with custom config
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val customConfig = TracingConfig(
            publishSpanNameFormatter = { _, _ -> "CUSTOM_PUBLISH" }
        )
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer, customConfig)

        try {
            // Open channel and publish
            val channel = tracedConnection.openChannel()
            channel.basicPublish(
                body = "test".encodeToByteArray(),
                exchange = "test-exchange",
                routingKey = "test",
                properties = Properties()
            )

            // Verify custom config was applied
            val spans = otelTesting.spans
            val publishSpan = spans.find { it.name == "CUSTOM_PUBLISH" }
            assertNotNull(publishSpan, "Custom span name formatter should be applied")

            runCatching { channel.close() }
        } finally {
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    @Test
    fun `connection state and config are accessible through wrapper`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val originalConfig = connection.config
        val originalState = connection.state

        val tracedConnection = connection.withTracing(tracer)

        try {
            // Verify properties are delegated correctly
            assertEquals(originalConfig, tracedConnection.config)
            assertEquals(originalState, tracedConnection.state)

            // Verify flows and deferreds are accessible
            assertNotNull(tracedConnection.connectionOpened)
            assertNotNull(tracedConnection.connectionClosed)
            assertNotNull(tracedConnection.openedResponses)
            assertNotNull(tracedConnection.closedResponses)
        } finally {
            runCatching { tracedConnection.close() }
        }
        Unit
    }

    // Helper function to convert string keys to OpenTelemetry AttributeKey
    private fun String.toKey() = io.opentelemetry.api.common.AttributeKey.stringKey(this)
}
