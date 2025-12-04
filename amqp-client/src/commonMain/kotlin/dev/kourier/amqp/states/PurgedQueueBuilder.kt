package dev.kourier.amqp.states

/**
 * Deletes all messages from a queue.
 */
class PurgedQueueBuilder {

    /**
     * Name of the queue.
     */
    var name: String = ""

    /**
     * Builds the [PurgedQueue] instance.
     *
     * @return The constructed [PurgedQueue].
     */
    fun build(): PurgedQueue {
        return PurgedQueue(
            name = name,
        )
    }

}
