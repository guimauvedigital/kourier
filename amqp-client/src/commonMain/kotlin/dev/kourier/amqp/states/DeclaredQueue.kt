package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Declares a queue.
 */
data class DeclaredQueue(
    /**
     * Name of the queue.
     */
    val name: String,
    /**
     * If enabled, creates a queue stored on disk; otherwise, transient.
     */
    val durable: Boolean,
    /**
     * If enabled, queue will be deleted when the channel is closed.
     */
    val exclusive: Boolean,
    /**
     * If enabled, queue will be deleted when the last consumer has stopped consuming.
     */
    val autoDelete: Boolean,
    /**
     * Additional arguments (check RabbitMQ documentation).
     */
    val arguments: Table,
)
