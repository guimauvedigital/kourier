package dev.kourier.amqp.states

/**
 * Tell the broker what to do with all unacknowledged messages.
 * Unacknowledged messages retrieved by `basicGet` are requeued regardless.
 */
class RecoveredMessagesBuilder {

    /**
     * Controls whether to requeue all messages after rejecting them.
     */
    var requeue: Boolean = false

    /**
     * Builds the [RecoveredMessages] instance.
     *
     * @return The constructed [RecoveredMessages].
     */
    fun build(): RecoveredMessages {
        return RecoveredMessages(
            requeue = requeue,
        )
    }

}
