package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Bind an exchange to another exchange.
 */
class BoundExchangeBuilder {

    /**
     * Output exchange.
     */
    var destination: String = ""

    /**
     * Input exchange.
     */
    var source: String = ""

    /**
     * Bind only to messages matching routingKey.
     */
    var routingKey: String = ""

    /**
     * Bind only to messages matching given options.
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [BoundExchange] instance.
     *
     * @return The constructed [BoundExchange].
     */
    fun build(): BoundExchange {
        return BoundExchange(
            destination = destination,
            source = source,
            routingKey = routingKey,
            arguments = arguments,
        )
    }

}
