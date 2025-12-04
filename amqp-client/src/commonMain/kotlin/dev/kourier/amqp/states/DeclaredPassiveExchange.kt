package dev.kourier.amqp.states

/**
 * Passively declares an exchange.
 */
data class DeclaredPassiveExchange(
    /**
     * Name of the exchange.
     */
    val name: String,
)
