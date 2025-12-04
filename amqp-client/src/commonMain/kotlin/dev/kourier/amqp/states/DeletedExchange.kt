package dev.kourier.amqp.states

/**
 * Delete an exchange.
 */
data class DeletedExchange(
    /**
     * Name of the exchange.
     */
    val name: String,
    /**
     * If enabled, exchange will be deleted only when it is not used.
     */
    val ifUnused: Boolean,
)
