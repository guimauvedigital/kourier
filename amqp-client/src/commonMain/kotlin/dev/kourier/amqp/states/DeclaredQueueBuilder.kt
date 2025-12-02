package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Declares a queue.
 */
class DeclaredQueueBuilder {

    /**
     * Name of the queue.
     */
    var name: String = ""

    /**
     * If enabled, creates a queue stored on disk; otherwise, transient.
     */
    var durable: Boolean = false

    /**
     * If enabled, queue will be deleted when the channel is closed.
     */
    var exclusive: Boolean = false

    /**
     * If enabled, queue will be deleted when the last consumer has stopped consuming.
     */
    var autoDelete: Boolean = false

    /**
     * Additional arguments (check RabbitMQ documentation).
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [DeclaredQueue] instance.
     *
     * @return The constructed [DeclaredQueue].
     */
    fun build(): DeclaredQueue {
        return DeclaredQueue(
            name = name,
            durable = durable,
            exclusive = exclusive,
            autoDelete = autoDelete,
            arguments = arguments,
        )
    }

}
