package dev.kourier.amqp.states

/**
 * Tell the broker what to do with all unacknowledged messages.
 * Unacknowledged messages retrieved by `basicGet` are requeued regardless.
 */
data class RecoveredMessages(
    /**
     * Controls whether to requeue all messages after rejecting them.
     */
    val requeue: Boolean,
)
