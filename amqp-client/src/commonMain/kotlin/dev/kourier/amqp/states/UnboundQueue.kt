package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Unbinds a queue from an exchange.
 */
data class UnboundQueue(
    /**
     * Name of the queue.
     */
    val queue: String,
    /**
     * Name of the exchange.
     */
    val exchange: String,
    /**
     * Unbind only from messages matching routingKey.
     */
    val routingKey: String,
    /**
     * Unbind only from messages matching given options.
     */
    val arguments: Table,
)
