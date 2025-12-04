package dev.kourier.amqp.states

/**
 * Passively declares a queue.
 */
data class DeclaredPassiveQueue(
    /**
     * Name of the queue.
     */
    val name: String,
)
