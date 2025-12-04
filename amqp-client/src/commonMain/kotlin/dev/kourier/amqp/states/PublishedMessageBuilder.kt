package dev.kourier.amqp.states

import dev.kourier.amqp.Properties

/**
 * Publish a ByteArray message to exchange or queue.
 */
class PublishedMessageBuilder {

    /**
     * Message payload that can be read from ByteArray.
     */
    var body: ByteArray = ByteArray(0)

    /**
     * Name of exchange on which the message is published. Can be empty.
     */
    var exchange: String = ""

    /**
     * Name of routingKey that will be attached to the message.
     * An exchange looks at the routingKey while deciding how the message has to be routed.
     * When exchange parameter is empty routingKey is used as queueName.
     */
    var routingKey: String = ""

    /**
     * When a published message cannot be routed to any queue and mandatory is true, the message will be returned to publisher.
     * Returned message must be handled with returnListener or returnConsumer.
     * When a published message cannot be routed to any queue and mandatory is false, the message is discarded or republished to an alternate exchange, if any.
     */
    var mandatory: Boolean = false

    /**
     * When matching queue has at least one or more consumers and immediate is set to true, message is delivered to them immediately.
     * When matching queue has zero active consumers and immediate is set to true, message is returned to publisher.
     * When matching queue has zero active consumers and immediate is set to false, message will be delivered to the queue.
     */
    var immediate: Boolean = false

    /**
     * Additional message properties (check amqp documentation).
     */
    var properties: Properties = Properties()

    /**
     * Builds the [PublishedMessage] instance.
     *
     * @return The constructed [PublishedMessage].
     */
    fun build(): PublishedMessage {
        return PublishedMessage(
            body = body,
            exchange = exchange,
            routingKey = routingKey,
            mandatory = mandatory,
            immediate = immediate,
            properties = properties,
        )
    }

}
