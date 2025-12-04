package dev.kourier.amqp.states

/**
 * Rollback a transaction.
 */
class RolledbackTransactionBuilder {

    /**
     * Builds the [RolledbackTransaction] instance.
     *
     * @return The constructed [RolledbackTransaction].
     */
    fun build(): RolledbackTransaction {
        return RolledbackTransaction()
    }

}
