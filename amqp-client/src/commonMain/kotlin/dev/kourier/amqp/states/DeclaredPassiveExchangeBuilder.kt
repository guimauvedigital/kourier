package dev.kourier.amqp.states

/**
 * Passively declares an exchange.
 */
class DeclaredPassiveExchangeBuilder {

    /**
     * Name of the exchange.
     */
    var name: String = ""

    /**
     * Builds the [DeclaredPassiveExchange] instance.
     *
     * @return The constructed [DeclaredPassiveExchange].
     */
    fun build(): DeclaredPassiveExchange {
        return DeclaredPassiveExchange(
            name = name,
        )
    }

}
