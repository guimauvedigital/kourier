package dev.kourier.amqp.states

import dev.kourier.amqp.Table

/**
 * Unbind an exchange from another exchange.
 */
class UnboundExchangeBuilder {

    /**
     * Output exchange.
     */
    var destination: String = ""

    /**
     * Input exchange.
     */
    var source: String = ""

    /**
     * Unbind only from messages matching routingKey.
     */
    var routingKey: String = ""

    /**
     * Unbind only from messages matching given options.
     */
    var arguments: Table = emptyMap()

    /**
     * Builds the [UnboundExchange] instance.
     *
     * @return The constructed [UnboundExchange].
     */
    fun build(): UnboundExchange {
        return UnboundExchange(
            destination = destination,
            source = source,
            routingKey = routingKey,
            arguments = arguments,
        )
    }

}
