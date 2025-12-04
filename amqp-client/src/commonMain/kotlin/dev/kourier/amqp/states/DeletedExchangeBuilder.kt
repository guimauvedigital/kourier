package dev.kourier.amqp.states

/**
 * Delete an exchange.
 */
class DeletedExchangeBuilder {

    /**
     * Name of the exchange.
     */
    var name: String = ""

    /**
     * If enabled, exchange will be deleted only when it is not used.
     */
    var ifUnused: Boolean = false

    /**
     * Builds the [DeletedExchange] instance.
     *
     * @return The constructed [DeletedExchange].
     */
    fun build(): DeletedExchange {
        return DeletedExchange(
            name = name,
            ifUnused = ifUnused,
        )
    }

}
