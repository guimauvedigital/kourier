package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Declare an exchange.
 */
class DeclaredExchangeBuilder {

    /**
     * Name of the exchange.
     */
    var name: String = ""

    /**
     * Type of the exchange.
     */
    var type: String = ""

    /**
     * If enabled, creates an exchange stored on disk; otherwise, transient.
     */
    var durable: Boolean = false

    /**
     * If enabled, exchange will be deleted when the last consumer has stopped consuming.
     */
    var autoDelete: Boolean = false

    /**
     * Whether the exchange cannot be directly published to by client.
     */
    var internal: Boolean = false

    /**
     * Additional arguments (check RabbitMQ documentation).
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [DeclaredExchange] instance.
     *
     * @return The constructed [DeclaredExchange].
     */
    fun build(): DeclaredExchange {
        return DeclaredExchange(
            name = name,
            type = type,
            durable = durable,
            autoDelete = autoDelete,
            internal = internal,
            arguments = arguments,
        )
    }

}
