package dev.kourier.amqp.states

/**
 * Get a single message from a queue.
 */
data class FetchedMessage(
    /**
     * Name of the queue.
     */
    val queue: String,
    /**
     * Controls whether message will be acked or nacked automatically (`true`) or manually (`false`).
     */
    val noAck: Boolean,
)
