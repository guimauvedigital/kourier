package dev.kourier.amqp.opentelemetry

/**
 * OpenTelemetry semantic conventions for messaging systems.
 *
 * Based on OpenTelemetry Semantic Conventions for Messaging:
 * https://opentelemetry.io/docs/specs/semconv/messaging/
 */
object SemanticAttributes {

    // Standard OpenTelemetry messaging attributes
    /**
     * The messaging system identifier (e.g., "rabbitmq", "kafka").
     */
    const val MESSAGING_SYSTEM = "messaging.system"

    /**
     * The name of the destination (exchange) to which the message was sent.
     */
    const val MESSAGING_DESTINATION_NAME = "messaging.destination.name"

    /**
     * The name of the source (queue) from which the message was received.
     */
    const val MESSAGING_SOURCE_NAME = "messaging.source.name"

    /**
     * The type of messaging operation (e.g., "publish", "receive", "process").
     */
    const val MESSAGING_OPERATION = "messaging.operation"

    /**
     * A unique identifier for the message.
     */
    const val MESSAGING_MESSAGE_ID = "messaging.message.id"

    /**
     * The conversation ID identifying the conversation to which the message belongs.
     */
    const val MESSAGING_CONVERSATION_ID = "messaging.conversation_id"

    /**
     * The size of the message body in bytes.
     */
    const val MESSAGING_MESSAGE_BODY_SIZE = "messaging.message.body.size"

    // RabbitMQ-specific attributes
    /**
     * RabbitMQ routing key used for message routing.
     */
    const val MESSAGING_RABBITMQ_ROUTING_KEY = "messaging.rabbitmq.routing_key"

    /**
     * RabbitMQ delivery mode (1 = non-persistent, 2 = persistent).
     */
    const val MESSAGING_RABBITMQ_DELIVERY_MODE = "messaging.rabbitmq.delivery_mode"

    /**
     * RabbitMQ exchange type (e.g., "direct", "topic", "fanout", "headers").
     */
    const val MESSAGING_RABBITMQ_EXCHANGE_TYPE = "messaging.rabbitmq.exchange.type"

    /**
     * Whether the message was published as mandatory.
     */
    const val MESSAGING_RABBITMQ_MANDATORY = "messaging.rabbitmq.mandatory"

    /**
     * Whether the message was published as immediate.
     */
    const val MESSAGING_RABBITMQ_IMMEDIATE = "messaging.rabbitmq.immediate"

    // Operation type constants
    /**
     * Operation type for publishing/sending a message.
     */
    const val OPERATION_PUBLISH = "publish"

    /**
     * Operation type for receiving/consuming a message.
     */
    const val OPERATION_RECEIVE = "receive"

    /**
     * Operation type for getting a single message.
     */
    const val OPERATION_GET = "get"

    /**
     * Operation type for processing a received message.
     */
    const val OPERATION_PROCESS = "process"

}
