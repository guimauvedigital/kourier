package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Unbinds a queue from an exchange.
 */
class UnboundQueueBuilder {

    /**
     * Name of the queue.
     */
    var queue: String = ""

    /**
     * Name of the exchange.
     */
    var exchange: String = ""

    /**
     * Unbind only from messages matching routingKey.
     */
    var routingKey: String = ""

    /**
     * Unbind only from messages matching given options.
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [UnboundQueue] instance.
     *
     * @return The constructed [UnboundQueue].
     */
    fun build(): UnboundQueue {
        return UnboundQueue(
            queue = queue,
            exchange = exchange,
            routingKey = routingKey,
            arguments = arguments,
        )
    }

}
