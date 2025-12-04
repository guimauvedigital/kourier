package dev.kourier.amqp.states

/**
 * Send a flow message to broker to start or stop sending messages to consumers.
 * Warning: Not supported by all brokers.
 */
class ControlledFlowBuilder {

    /**
     * Flow enabled or disabled.
     */
    var active: Boolean = false

    /**
     * Builds the [ControlledFlow] instance.
     *
     * @return The constructed [ControlledFlow].
     */
    fun build(): ControlledFlow {
        return ControlledFlow(
            active = active,
        )
    }

}
