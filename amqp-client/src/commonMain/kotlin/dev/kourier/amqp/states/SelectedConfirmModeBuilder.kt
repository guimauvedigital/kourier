package dev.kourier.amqp.states

/**
 * Set channel in publish confirm mode, each published message will be acked or nacked.
 */
class SelectedConfirmModeBuilder {

    /**
     * Builds the [SelectedConfirmMode] instance.
     *
     * @return The constructed [SelectedConfirmMode].
     */
    fun build(): SelectedConfirmMode {
        return SelectedConfirmMode()
    }

}
