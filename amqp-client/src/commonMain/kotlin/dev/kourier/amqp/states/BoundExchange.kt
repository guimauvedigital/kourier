package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Bind an exchange to another exchange.
 */
data class BoundExchange(
    /**
     * Output exchange.
     */
    val destination: String,
    /**
     * Input exchange.
     */
    val source: String,
    /**
     * Bind only to messages matching routingKey.
     */
    val routingKey: String,
    /**
     * Bind only to messages matching given options.
     */
    val arguments: Table,
)
