package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.BuiltinExchangeType
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
            channel.exchangeDeclare("test-exchange-attributes", type = BuiltinExchangeType.DIRECT)

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
            channel.exchangeDeclare("test-exchange-headers", type = BuiltinExchangeType.DIRECT)

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
            // Declare queue and exchange before consuming
            channel.queueDeclare("test-queue-consume")
            channel.exchangeDeclare("test-exchange-consume", type = BuiltinExchangeType.DIRECT)
            channel.queueBind("test-queue-consume", "test-exchange-consume", "test-key")

            // Set up consumer and wait for delivery
            var deliveryReceived = false
            val consumeOk = channel.basicConsume(
                queue = "test-queue-consume",
                onDelivery = { delivery ->
                    deliveryReceived = true
                }
            )

            assertNotNull(consumeOk)

            // Publish a message to trigger the consumer
            channel.basicPublish(
                body = "test message for consumer".encodeToByteArray(),
                exchange = "test-exchange-consume",
                routingKey = "test-key",
                properties = Properties(messageId = "consume-test-123")
            )

            // Wait a bit for message delivery
            kotlinx.coroutines.delay(500)

            // Verify delivery was received
            assertTrue(deliveryReceived, "Message should have been delivered")

            // Verify consumer span was created with correct attributes (after callback completes)
            val spans = otelTesting.spans
            val consumerSpan = spans.find { it.name == "test-queue-consume receive" }

            assertNotNull(consumerSpan, "Consumer span should be created")
            assertEquals(SpanKind.CONSUMER, consumerSpan.kind)

            val attributes = consumerSpan.attributes.asMap()
            assertEquals("rabbitmq", attributes[stringKey(SemanticAttributes.MESSAGING_SYSTEM)])
            assertEquals("receive", attributes[stringKey(SemanticAttributes.MESSAGING_OPERATION)])
            assertEquals("test-queue-consume", attributes[stringKey(SemanticAttributes.MESSAGING_SOURCE_NAME)])
            assertEquals("test-exchange-consume", attributes[stringKey(SemanticAttributes.MESSAGING_DESTINATION_NAME)])
            assertEquals("test-key", attributes[stringKey(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY)])
        } finally {
            runCatching { channel.close() }
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `basicConsume propagates trace context from published message`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Declare resources
            channel.queueDeclare("test-queue-propagation")
            channel.exchangeDeclare("test-exchange-propagation", type = BuiltinExchangeType.DIRECT)
            channel.queueBind("test-queue-propagation", "test-exchange-propagation", "test-key")

            var deliveryReceived = false
            var messageHasTraceHeaders = false
            var traceparentValue: String? = null

            // Set up consumer
            channel.basicConsume(
                queue = "test-queue-propagation",
                onDelivery = { delivery ->
                    deliveryReceived = true
                    // Check if trace context headers are present in the delivered message
                    val headers = delivery.message.properties.headers
                    messageHasTraceHeaders = headers?.containsKey("traceparent") == true
                    traceparentValue = (headers?.get("traceparent") as? dev.kourier.amqp.Field.LongString)?.value
                }
            )

            // Publish a message (this creates a producer span)
            channel.basicPublish(
                body = "propagation test".encodeToByteArray(),
                exchange = "test-exchange-propagation",
                routingKey = "test-key",
                properties = Properties()
            )

            // Wait for delivery
            kotlinx.coroutines.delay(500)

            // Verify delivery happened
            assertTrue(deliveryReceived, "Message should have been delivered")
            assertTrue(messageHasTraceHeaders, "Message should contain trace context headers")
            assertNotNull(traceparentValue, "traceparent header should have a value")

            // Verify traceparent format is valid (00-{32 hex chars}-{16 hex chars}-{2 hex chars})
            val parts = traceparentValue!!.split("-")
            assertEquals(4, parts.size, "traceparent should have 4 parts")
            assertEquals("00", parts[0], "version should be 00")
            assertEquals(32, parts[1].length, "trace-id should be 32 characters")
            assertEquals(16, parts[2].length, "span-id should be 16 characters")
            assertEquals(2, parts[3].length, "trace-flags should be 2 characters")

            // Verify both producer and consumer spans were created
            val spans = otelTesting.spans
            val publishSpan = spans.find { it.name == "test-exchange-propagation send" }
            val consumerSpan = spans.find { it.name == "test-queue-propagation receive" }

            assertNotNull(publishSpan, "Publish span should be created")
            assertNotNull(consumerSpan, "Consumer span should be created")

            // Note: We verify trace context injection/extraction work correctly.
            // The parent-child relationship is tested in SpanPropagatorTest unit tests.
        } finally {
            runCatching { channel.close() }
            runCatching { connection.close() }
        }
        Unit
    }

    @Test
    fun `basicConsume handles errors in delivery handler`() = runBlocking {
        // Setup
        val tracer = otelTesting.openTelemetry.getTracer("test")
        val connection = createAMQPConnection(this) {}
        val tracedConnection = connection.withTracing(tracer)
        val channel = tracedConnection.openChannel()

        try {
            // Declare resources
            channel.queueDeclare("test-queue-error")
            channel.exchangeDeclare("test-exchange-error", type = BuiltinExchangeType.DIRECT)
            channel.queueBind("test-queue-error", "test-exchange-error", "test-key")

            var errorThrown = false

            // Set up consumer that throws an error
            channel.basicConsume(
                queue = "test-queue-error",
                onDelivery = { delivery ->
                    errorThrown = true
                    throw RuntimeException("Test error in delivery handler")
                }
            )

            // Publish a message
            channel.basicPublish(
                body = "error test".encodeToByteArray(),
                exchange = "test-exchange-error",
                routingKey = "test-key",
                properties = Properties()
            )

            // Wait for delivery and error
            kotlinx.coroutines.delay(500)

            // Verify error was thrown
            assertTrue(errorThrown, "Error should have been thrown")

            // Verify error span was recorded (after callback completes)
            val spans = otelTesting.spans
            val errorSpan = spans.find {
                it.name == "test-queue-error receive" &&
                it.status.statusCode == StatusCode.ERROR
            }

            assertNotNull(errorSpan, "Error span should have been recorded")
            assertTrue(errorSpan.status.description.contains("Test error in delivery handler"))
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
            // Declare queue before getting
            channel.queueDeclare("test-queue-get")

            // Execute get (will return empty response without real broker)
            val response = channel.basicGet("test-queue-get", noAck = false)

            // Verify span was created
            val spans = otelTesting.spans
            val getSpan = spans.find { it.name == "test-queue-get get" }

            assertNotNull(getSpan, "Consumer span should be created for basicGet")
            assertEquals(SpanKind.CONSUMER, getSpan.kind)

            val attributes = getSpan.attributes.asMap()
            assertEquals("rabbitmq", attributes[stringKey(SemanticAttributes.MESSAGING_SYSTEM)])
            assertEquals("get", attributes[stringKey(SemanticAttributes.MESSAGING_OPERATION)])
            assertEquals("test-queue-get", attributes[stringKey(SemanticAttributes.MESSAGING_SOURCE_NAME)])
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
            channel.exchangeDeclare("my-exchange", type = BuiltinExchangeType.DIRECT)

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
            channel.exchangeDeclare("test-exchange-mgmt", type = BuiltinExchangeType.DIRECT, durable = true)
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
            channel.exchangeDeclare("test-exchange-nomgmt", type = BuiltinExchangeType.DIRECT)

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
