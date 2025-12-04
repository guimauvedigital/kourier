package dev.kourier.amqp.states

/**
 * Set channel in transaction mode.
 */
class SelectedTransactionModeBuilder {

    /**
     * Builds the [SelectedTransactionMode] instance.
     *
     * @return The constructed [SelectedTransactionMode].
     */
    fun build(): SelectedTransactionMode {
        return SelectedTransactionMode()
    }

}
