package dev.kourier.amqp.states

/**
 * Get a single message from a queue.
 */
class FetchedMessageBuilder {

    /**
     * Name of the queue.
     */
    var queue: String = ""

    /**
     * Controls whether message will be acked or nacked automatically (`true`) or manually (`false`).
     */
    var noAck: Boolean = false

    /**
     * Builds the [FetchedMessage] instance.
     *
     * @return The constructed [FetchedMessage].
     */
    fun build(): FetchedMessage {
        return FetchedMessage(
            queue = queue,
            noAck = noAck,
        )
    }

}
