package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Properties
import dev.kourier.amqp.Table
import dev.kourier.amqp.channel.AMQPChannel
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext

/**
 * OpenTelemetry-instrumented wrapper around [AMQPChannel].
 *
 * This class automatically creates spans for message publishing and consumption operations,
 * and propagates trace context through AMQP message headers using W3C Trace Context format.
 *
 * @property delegate The underlying AMQP channel to wrap.
 * @property tracer The OpenTelemetry tracer to use for creating spans.
 * @property config Configuration for tracing behavior.
 */
class OpenTelemetryAMQPChannel(
    private val delegate: AMQPChannel,
    private val tracer: Tracer,
    private val config: TracingConfig = TracingConfig.default(),
) : AMQPChannel by delegate {

    override suspend fun open(): AMQPResponse.Channel.Opened {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("channel.open", SpanKind.CLIENT) {
                delegate.open()
            }
        } else {
            delegate.open()
        }
    }

    override suspend fun close(reason: String, code: UShort): AMQPResponse.Channel.Closed {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("channel.close", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.operation", "close")
                if (reason.isNotEmpty()) {
                    span.setAttribute("messaging.close_reason", reason)
                }
                span.setAttribute("messaging.close_code", code.toLong())
                delegate.close(reason, code)
            }
        } else {
            delegate.close(reason, code)
        }
    }

    override suspend fun basicPublish(
        body: ByteArray,
        exchange: String,
        routingKey: String,
        mandatory: Boolean,
        immediate: Boolean,
        properties: Properties,
    ): AMQPResponse.Channel.Basic.Published {
        val spanName = config.publishSpanNameFormatter(exchange, routingKey)

        return executeInSpan(spanName, SpanKind.PRODUCER) { span ->
            // Set semantic attributes
            span.setAttribute(SemanticAttributes.MESSAGING_SYSTEM, "rabbitmq")
            span.setAttribute(SemanticAttributes.MESSAGING_OPERATION, SemanticAttributes.OPERATION_PUBLISH)
            span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_BODY_SIZE, body.size.toLong())

            if (exchange.isNotEmpty()) {
                span.setAttribute(SemanticAttributes.MESSAGING_DESTINATION_NAME, exchange)
            }
            if (routingKey.isNotEmpty()) {
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY, routingKey)
            }
            if (mandatory) {
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_MANDATORY, true)
            }
            if (immediate) {
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_IMMEDIATE, true)
            }

            // Set attributes from properties
            properties.messageId?.let { span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_ID, it) }
            properties.correlationId?.let { span.setAttribute(SemanticAttributes.MESSAGING_CONVERSATION_ID, it) }
            properties.deliveryMode?.let {
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_DELIVERY_MODE, it.toLong())
            }

            // Capture message body if configured
            if (config.captureMessageBody && body.isNotEmpty()) {
                val bodyToCapture = if (body.size > config.maxBodySizeToCapture) {
                    body.copyOfRange(0, config.maxBodySizeToCapture)
                } else {
                    body
                }
                span.setAttribute("messaging.message.body", bodyToCapture.decodeToString())
            }

            // Inject trace context into message headers
            val currentContext = Context.current()
            val updatedProperties = SpanPropagator.inject(properties, currentContext)

            delegate.basicPublish(body, exchange, routingKey, mandatory, immediate, updatedProperties)
        }
    }

    override suspend fun basicGet(queue: String, noAck: Boolean): AMQPResponse.Channel.Message.Get {
        val spanName = "${queue} get"

        return executeInSpan(spanName, SpanKind.CONSUMER) { span ->
            span.setAttribute(SemanticAttributes.MESSAGING_SYSTEM, "rabbitmq")
            span.setAttribute(SemanticAttributes.MESSAGING_OPERATION, SemanticAttributes.OPERATION_GET)
            span.setAttribute(SemanticAttributes.MESSAGING_SOURCE_NAME, queue)

            val response = delegate.basicGet(queue, noAck)

            // Only process if message is present
            response.message?.let { message ->
                // Extract trace context and link to parent span
                val parentContext = SpanPropagator.extract(message.properties)
                if (parentContext != Context.root()) {
                    val parentSpan = Span.fromContext(parentContext)
                    if (parentSpan.spanContext.isValid) {
                        span.addLink(parentSpan.spanContext)
                    }
                }

                // Set message attributes
                span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_ID, message.deliveryTag.toString())
                message.properties.correlationId?.let {
                    span.setAttribute(SemanticAttributes.MESSAGING_CONVERSATION_ID, it)
                }
                span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_BODY_SIZE, message.body.size.toLong())
            }

            response
        }
    }

    override suspend fun basicConsume(
        queue: String,
        consumerTag: String,
        noAck: Boolean,
        exclusive: Boolean,
        arguments: Table,
        onDelivery: suspend (AMQPResponse.Channel.Message.Delivery) -> Unit,
        onCanceled: suspend (AMQPResponse.Channel) -> Unit,
    ): AMQPResponse.Channel.Basic.ConsumeOk {
        // Wrap the delivery handler with tracing
        val wrappedOnDelivery: suspend (AMQPResponse.Channel.Message.Delivery) -> Unit = { delivery ->
            handleDeliveryWithTracing(queue, delivery, onDelivery)
        }

        return delegate.basicConsume(queue, consumerTag, noAck, exclusive, arguments, wrappedOnDelivery, onCanceled)
    }

    /**
     * Handles a message delivery with tracing.
     * Extracts trace context from message headers and creates a consumer span.
     */
    private suspend fun handleDeliveryWithTracing(
        queue: String,
        delivery: AMQPResponse.Channel.Message.Delivery,
        handler: suspend (AMQPResponse.Channel.Message.Delivery) -> Unit,
    ) {
        val spanName = config.consumeSpanNameFormatter(queue)

        // Extract trace context from message headers
        val parentContext = SpanPropagator.extract(delivery.message.properties)

        val span = tracer.spanBuilder(spanName)
            .setSpanKind(SpanKind.CONSUMER)
            .setParent(parentContext)
            .startSpan()

        withContext(span.asContextElement()) {
            try {
                // Set semantic attributes
                span.setAttribute(SemanticAttributes.MESSAGING_SYSTEM, "rabbitmq")
                span.setAttribute(SemanticAttributes.MESSAGING_OPERATION, SemanticAttributes.OPERATION_RECEIVE)
                span.setAttribute(SemanticAttributes.MESSAGING_SOURCE_NAME, queue)
                span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_ID, delivery.message.deliveryTag.toString())
                span.setAttribute(SemanticAttributes.MESSAGING_MESSAGE_BODY_SIZE, delivery.message.body.size.toLong())

                delivery.message.properties.correlationId?.let {
                    span.setAttribute(SemanticAttributes.MESSAGING_CONVERSATION_ID, it)
                }
                span.setAttribute(SemanticAttributes.MESSAGING_DESTINATION_NAME, delivery.message.exchange)
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY, delivery.message.routingKey)

                // Capture message body if configured
                if (config.captureMessageBody && delivery.message.body.isNotEmpty()) {
                    val bodyToCapture = if (delivery.message.body.size > config.maxBodySizeToCapture) {
                        delivery.message.body.copyOfRange(0, config.maxBodySizeToCapture)
                    } else {
                        delivery.message.body
                    }
                    span.setAttribute("messaging.message.body", bodyToCapture.decodeToString())
                }

                handler(delivery)

                span.setStatus(StatusCode.OK)
            } catch (e: Exception) {
                span.recordException(e)
                span.setStatus(StatusCode.ERROR, e.message ?: "Error processing message")
                throw e
            } finally {
                span.end()
            }
        }
    }

    override suspend fun basicQos(count: UShort, global: Boolean): AMQPResponse.Channel.Basic.QosOk {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("channel.qos", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.qos.prefetch_count", count.toLong())
                span.setAttribute("messaging.qos.global", global)
                delegate.basicQos(count, global)
            }
        } else {
            delegate.basicQos(count, global)
        }
    }


    override suspend fun queueDeclare(
        name: String,
        durable: Boolean,
        exclusive: Boolean,
        autoDelete: Boolean,
        arguments: Table,
    ): AMQPResponse.Channel.Queue.Declared {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("queue.declare", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.queue.name", name)
                span.setAttribute("messaging.queue.durable", durable)
                span.setAttribute("messaging.queue.exclusive", exclusive)
                span.setAttribute("messaging.queue.auto_delete", autoDelete)
                delegate.queueDeclare(name, durable, exclusive, autoDelete, arguments)
            }
        } else {
            delegate.queueDeclare(name, durable, exclusive, autoDelete, arguments)
        }
    }


    override suspend fun queueDelete(
        name: String,
        ifUnused: Boolean,
        ifEmpty: Boolean,
    ): AMQPResponse.Channel.Queue.Deleted {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("queue.delete", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.queue.name", name)
                delegate.queueDelete(name, ifUnused, ifEmpty)
            }
        } else {
            delegate.queueDelete(name, ifUnused, ifEmpty)
        }
    }


    override suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Queue.Bound {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("queue.bind", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.queue.name", queue)
                span.setAttribute("messaging.exchange.name", exchange)
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY, routingKey)
                delegate.queueBind(queue, exchange, routingKey, arguments)
            }
        } else {
            delegate.queueBind(queue, exchange, routingKey, arguments)
        }
    }


    override suspend fun exchangeDeclare(
        name: String,
        type: String,
        durable: Boolean,
        autoDelete: Boolean,
        internal: Boolean,
        arguments: Table,
    ): AMQPResponse.Channel.Exchange.Declared {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("exchange.declare", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.exchange.name", name)
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_EXCHANGE_TYPE, type)
                span.setAttribute("messaging.exchange.durable", durable)
                span.setAttribute("messaging.exchange.auto_delete", autoDelete)
                span.setAttribute("messaging.exchange.internal", internal)
                delegate.exchangeDeclare(name, type, durable, autoDelete, internal, arguments)
            }
        } else {
            delegate.exchangeDeclare(name, type, durable, autoDelete, internal, arguments)
        }
    }


    override suspend fun exchangeDelete(name: String, ifUnused: Boolean): AMQPResponse.Channel.Exchange.Deleted {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("exchange.delete", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.exchange.name", name)
                delegate.exchangeDelete(name, ifUnused)
            }
        } else {
            delegate.exchangeDelete(name, ifUnused)
        }
    }

    override suspend fun exchangeBind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Exchange.Bound {
        return if (config.traceChannelManagementOperations) {
            executeInSpan("exchange.bind", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.exchange.destination", destination)
                span.setAttribute("messaging.exchange.source", source)
                span.setAttribute(SemanticAttributes.MESSAGING_RABBITMQ_ROUTING_KEY, routingKey)
                delegate.exchangeBind(destination, source, routingKey, arguments)
            }
        } else {
            delegate.exchangeBind(destination, source, routingKey, arguments)
        }
    }


    /**
     * Helper function to execute code within a span.
     */
    private suspend fun <T> executeInSpan(
        spanName: String,
        spanKind: SpanKind,
        block: suspend (Span) -> T,
    ): T {
        val span = tracer.spanBuilder(spanName)
            .setSpanKind(spanKind)
            .startSpan()
        return try {
            withContext(span.asContextElement()) {
                val result = block(span)
                span.setStatus(StatusCode.OK)
                result
            }
        } catch (e: Exception) {
            span.recordException(e)
            span.setStatus(StatusCode.ERROR, e.message ?: "Error in operation")
            throw e
        } finally {
            span.end()
        }
    }

}
