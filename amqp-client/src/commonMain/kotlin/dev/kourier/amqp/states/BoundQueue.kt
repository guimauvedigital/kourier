package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Binds a queue to an exchange.
 */
data class BoundQueue(
    /**
     * Name of the queue.
     */
    val queue: String,
    /**
     * Name of the exchange.
     */
    val exchange: String,
    /**
     * Bind only to messages matching routingKey.
     */
    val routingKey: String,
    /**
     * Bind only to messages matching given options.
     */
    val arguments: Table,
)
