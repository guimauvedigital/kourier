package dev.kourier.amqp.states

import dev.kourier.amqp.Properties

/**
 * Publish a ByteArray message to exchange or queue.
 */
data class PublishedMessage(
    /**
     * Message payload that can be read from ByteArray.
     */
    val body: ByteArray,
    /**
     * Name of exchange on which the message is published. Can be empty.
     */
    val exchange: String,
    /**
     * Name of routingKey that will be attached to the message.
     * An exchange looks at the routingKey while deciding how the message has to be routed.
     * When exchange parameter is empty routingKey is used as queueName.
     */
    val routingKey: String,
    /**
     * When a published message cannot be routed to any queue and mandatory is true, the message will be returned to publisher.
     * Returned message must be handled with returnListener or returnConsumer.
     * When a published message cannot be routed to any queue and mandatory is false, the message is discarded or republished to an alternate exchange, if any.
     */
    val mandatory: Boolean,
    /**
     * When matching queue has at least one or more consumers and immediate is set to true, message is delivered to them immediately.
     * When matching queue has zero active consumers and immediate is set to true, message is returned to publisher.
     * When matching queue has zero active consumers and immediate is set to false, message will be delivered to the queue.
     */
    val immediate: Boolean,
    /**
     * Additional message properties (check amqp documentation).
     */
    val properties: Properties,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PublishedMessage

        if (!body.contentEquals(other.body)) return false
        if (exchange != other.exchange) return false
        if (routingKey != other.routingKey) return false
        if (mandatory != other.mandatory) return false
        if (immediate != other.immediate) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = body.contentHashCode()
        result = 31 * result + exchange.hashCode()
        result = 31 * result + routingKey.hashCode()
        result = 31 * result + mandatory.hashCode()
        result = 31 * result + immediate.hashCode()
        result = 31 * result + properties.hashCode()
        return result
    }

}
