package dev.kourier.amqp.states

/**
 * Deletes all messages from a queue.
 */
data class PurgedQueue(
    /**
     * Name of the queue.
     */
    val name: String,
)
