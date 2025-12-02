package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Declare an exchange.
 */
data class DeclaredExchange(
    /**
     * Name of the exchange.
     */
    val name: String,
    /**
     * Type of the exchange.
     */
    val type: String,
    /**
     * If enabled, creates an exchange stored on disk; otherwise, transient.
     */
    val durable: Boolean,
    /**
     * If enabled, exchange will be deleted when the last consumer has stopped consuming.
     */
    val autoDelete: Boolean,
    /**
     * Whether the exchange cannot be directly published to by client.
     */
    val internal: Boolean,
    /**
     * Additional arguments (check RabbitMQ documentation).
     */
    val arguments: Table,
)
