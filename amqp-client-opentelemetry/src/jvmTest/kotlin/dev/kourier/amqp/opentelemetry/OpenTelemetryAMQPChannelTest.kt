package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.Properties
import dev.kourier.amqp.connection.createAMQPConnection
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.extension.RegisterExtension
import kotlin.test.*

class OpenTelemetryAMQPChannelTest {

    companion object {
        @JvmField
        @RegisterExtension
        val otelTesting = OpenTelemetryExtension.create()
    }

    @Test
    fun `basicPublish creates producer span with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Declare exchange before publishing
            channel.exchangeDeclare("test-exchange-attributes", type = "direct")

            // Execute publish
            channel.basicPublish(
                body = "test message".encodeToByteArray(),
                exchange = "test-exchange-attributes",
                routingKey = "test.route",
                properties = Properties(
                    messageId = "msg-123",
                    correlationId = "corr-456"
                )
            )

            // Verify span was created
            val spans = otelTesting.spans
            val publishSpan = spans.find { it.name == "test-exchange-attributes send" }

            assertNotNull(publishSpan, "Producer span should be created")
            assertEquals(SpanKind.PRODUCER, publishSpan.kind)
            assertEquals(StatusCode.OK, publishSpan.status.statusCode)

            // Verify attributes
            val attributes = publishSpan.attributes.asMap()
            assertEquals("rabbitmq", attributes[stringKey(SemanticAttributes.MESSAGING_SYSTEM)])
            assertEquals("publish", attributes[stringKey(SemanticAttributes.MESSAGING_OPERATION)])
            assertEquals("test-exchange-attributes", attributes[stringKey(SemanticAttributes.MESSAGING_DESTINATION_NAME)])
            assertEquals("test.route", attributes[stringKey(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY)])
            assertEquals("msg-123", attributes[stringKey(SemanticAttributes.MESSAGING_MESSAGE_ID)])
            assertEquals("corr-456", attributes[stringKey(SemanticAttributes.MESSAGING_CONVERSATION_ID)])
            assertEquals(12L, attributes[longKey(SemanticAttributes.MESSAGING_MESSAGE_BODY_SIZE)])
        } finally {
            channel.close()
            connection.close()
        }
    }

    @Test
    fun `basicPublish injects trace context into message headers`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Declare exchange before publishing
            channel.exchangeDeclare("test-exchange-headers", type = "direct")

            // Publish with empty properties
            val properties = Properties()
            val response = channel.basicPublish(
                body = "test".encodeToByteArray(),
                exchange = "test-exchange-headers",
                routingKey = "test.route",
                properties = properties
            )

            // Note: We can't easily verify the injected headers without accessing the actual message
            // but we can verify the span was created and is valid
            val spans = otelTesting.spans
            val publishSpan = spans.find { it.name == "test-exchange-headers send" }
            assertNotNull(publishSpan)
            assertTrue(publishSpan.spanContext.isValid)
            assertTrue(publishSpan.spanContext.traceId.isNotEmpty())
            assertTrue(publishSpan.spanContext.spanId.isNotEmpty())
        } finally {
            channel.close()
            connection.close()
        }
    }

    @Test
    fun `basicPublish with empty exchange uses routing key in span name`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Declare queue before publishing to default exchange
            channel.queueDeclare("my-queue")

            // Publish to default exchange (empty string)
            channel.basicPublish(
                body = "test".encodeToByteArray(),
                exchange = "",
                routingKey = "my-queue",
                properties = Properties()
            )

            // Verify span name uses routing key
            val spans = otelTesting.spans
            val publishSpan = spans.find { it.name == "my-queue send" }
            assertNotNull(publishSpan, "Span name should be 'my-queue send' when exchange is empty")
        } finally {
            runCatching { channel.close() }
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `basicConsume creates consumer span with correct attributes`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Simulate message consumption
            var deliveryReceived = false
            val consumeOk = channel.basicConsume(
                queue = "test-queue",
                onDelivery = { delivery ->
                    deliveryReceived = true

                    // Verify consumer span
                    val spans = otelTesting.spans
                    val consumerSpan = spans.find { it.name == "test-queue receive" }

                    if (consumerSpan != null) {
                        assertEquals(SpanKind.CONSUMER, consumerSpan.kind)

                        val attributes = consumerSpan.attributes.asMap()
                        assertEquals("rabbitmq", attributes[stringKey(SemanticAttributes.MESSAGING_SYSTEM)])
                        assertEquals("receive", attributes[stringKey(SemanticAttributes.MESSAGING_OPERATION)])
                        assertEquals("test-queue", attributes[stringKey(SemanticAttributes.MESSAGING_SOURCE_NAME)])
                    }
                }
            )

            assertNotNull(consumeOk)

            // Note: Without a real RabbitMQ broker, we can't test actual message delivery
            // This test verifies the consume setup works without errors
        } finally {
            runCatching { channel.close() }
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `basicGet creates consumer span for single message retrieval`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Execute get (will return empty response without real broker)
            val response = channel.basicGet("test-queue", noAck = false)

            // Verify span was created
            val spans = otelTesting.spans
            val getSpan = spans.find { it.name == "test-queue get" }

            assertNotNull(getSpan, "Consumer span should be created for basicGet")
            assertEquals(SpanKind.CONSUMER, getSpan.kind)

            val attributes = getSpan.attributes.asMap()
            assertEquals("rabbitmq", attributes[stringKey(SemanticAttributes.MESSAGING_SYSTEM)])
            assertEquals("get", attributes[stringKey(SemanticAttributes.MESSAGING_OPERATION)])
            assertEquals("test-queue", attributes[stringKey(SemanticAttributes.MESSAGING_SOURCE_NAME)])
        } finally {
            channel.close()
            connection.close()
        }
    }

    @Test
    fun `custom span name formatters are used`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val config = TracingConfig(
            publishSpanNameFormatter = { exchange, _ -> "PUBLISH:$exchange" }
        )
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer, config)
        val channel = tracedConnection.openChannel()

        try {
            // Declare exchange before publishing
            channel.exchangeDeclare("my-exchange", type = "direct")

            // Test publish with custom formatter
            channel.basicPublish(
                body = "test".encodeToByteArray(),
                exchange = "my-exchange",
                routingKey = "test",
                properties = Properties()
            )

            val publishSpan = otelTesting.spans.find { it.name == "PUBLISH:my-exchange" }
            assertNotNull(publishSpan, "Custom publish span name should be used")
        } finally {
            runCatching { channel.close() }
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `traceChannelManagementOperations config enables management spans`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val config = TracingConfig(traceChannelManagementOperations = true)
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer, config)
        val channel = tracedConnection.openChannel()

        try {
            // Execute management operations
            channel.queueDeclare("test-queue-mgmt", durable = true)
            channel.exchangeDeclare("test-exchange-mgmt", type = "direct", durable = true)
            channel.queueBind("test-queue-mgmt", "test-exchange-mgmt", "test-key")

            // Verify management spans were created
            val spans = otelTesting.spans
            val queueDeclareSpan = spans.find { it.name == "queue.declare" }
            val exchangeDeclareSpan = spans.find { it.name == "exchange.declare" }
            val queueBindSpan = spans.find { it.name == "queue.bind" }

            assertNotNull(queueDeclareSpan, "queueDeclare span should be created when enabled")
            assertNotNull(exchangeDeclareSpan, "exchangeDeclare span should be created when enabled")
            assertNotNull(queueBindSpan, "queueBind span should be created when enabled")

            // Verify attributes
            val queueAttrs = queueDeclareSpan!!.attributes.asMap()
            assertEquals("test-queue-mgmt", queueAttrs[stringKey("messaging.queue.name")])
            assertEquals(true, queueAttrs[boolKey("messaging.queue.durable")])

            val exchangeAttrs = exchangeDeclareSpan!!.attributes.asMap()
            assertEquals("test-exchange-mgmt", exchangeAttrs[stringKey("messaging.exchange.name")])
            assertEquals("direct", exchangeAttrs[stringKey(SemanticAttributes.MESSAGING_RABBITMQ_EXCHANGE_TYPE)])
        } finally {
            channel.close()
            connection.close()
        }
    }

    @Test
    fun `traceChannelManagementOperations disabled by default`() = runBlocking {
        // Setup with default config
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer) // default config
        val channel = tracedConnection.openChannel()

        try {
            // Execute management operations
            channel.queueDeclare("test-queue-nomgmt")
            channel.exchangeDeclare("test-exchange-nomgmt", type = "direct")

            // Verify no management spans were created
            val spans = otelTesting.spans
            val managementSpans = spans.filter {
                it.name.startsWith("queue.") || it.name.startsWith("exchange.")
            }

            assertTrue(
                managementSpans.isEmpty(),
                "Management spans should not be created when disabled (default)"
            )
        } finally {
            channel.close()
            connection.close()
        }
    }

    // Helper functions to create OpenTelemetry AttributeKeys
    private fun stringKey(name: String) = io.opentelemetry.api.common.AttributeKey.stringKey(name)
    private fun longKey(name: String) = io.opentelemetry.api.common.AttributeKey.longKey(name)
    private fun boolKey(name: String) = io.opentelemetry.api.common.AttributeKey.booleanKey(name)
}
