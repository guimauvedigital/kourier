package dev.kourier.amqp.states

/**
 * Commit a transaction.
 */
class CommittedTransactionBuilder {

    /**
     * Builds the [CommittedTransaction] instance.
     *
     * @return The constructed [CommittedTransaction].
     */
    fun build(): CommittedTransaction {
        return CommittedTransaction()
    }

}
