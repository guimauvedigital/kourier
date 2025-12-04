package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Unbind an exchange from another exchange.
 */
data class UnboundExchange(
    /**
     * Output exchange.
     */
    val destination: String,
    /**
     * Input exchange.
     */
    val source: String,
    /**
     * Unbind only from messages matching routingKey.
     */
    val routingKey: String,
    /**
     * Unbind only from messages matching given options.
     */
    val arguments: Table,
)
