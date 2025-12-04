package dev.kourier.amqp.states

/**
 * Deletes a queue.
 */
class DeletedQueueBuilder {

    /**
     * Name of the queue.
     */
    var name: String = ""

    /**
     * If enabled, queue will be deleted only when there are no consumers subscribed to it.
     */
    var ifUnused: Boolean = false

    /**
     * If enabled, queue will be deleted only when it's empty.
     */
    var ifEmpty: Boolean = false

    /**
     * Builds the [DeletedQueue] instance.
     *
     * @return The constructed [DeletedQueue].
     */
    fun build(): DeletedQueue {
        return DeletedQueue(
            name = name,
            ifUnused = ifUnused,
            ifEmpty = ifEmpty,
        )
    }

}
