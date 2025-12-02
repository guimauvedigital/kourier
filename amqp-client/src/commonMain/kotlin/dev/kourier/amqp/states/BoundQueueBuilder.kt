package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Binds a queue to an exchange.
 */
class BoundQueueBuilder {

    /**
     * Name of the queue.
     */
    var queue: String = ""

    /**
     * Name of the exchange.
     */
    var exchange: String = ""

    /**
     * Bind only to messages matching routingKey.
     */
    var routingKey: String = ""

    /**
     * Bind only to messages matching given options.
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [BoundQueue] instance.
     *
     * @return The constructed [BoundQueue].
     */
    fun build(): BoundQueue {
        return BoundQueue(
            queue = queue,
            exchange = exchange,
            routingKey = routingKey,
            arguments = arguments,
        )
    }

}
