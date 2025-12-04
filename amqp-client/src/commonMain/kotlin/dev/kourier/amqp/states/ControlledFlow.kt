package dev.kourier.amqp.states

/**
 * Send a flow message to broker to start or stop sending messages to consumers.
 * Warning: Not supported by all brokers.
 */
data class ControlledFlow(
    /**
     * Flow enabled or disabled.
     */
    val active: Boolean,
)
