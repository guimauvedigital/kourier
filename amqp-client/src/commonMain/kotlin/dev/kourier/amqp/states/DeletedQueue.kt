package dev.kourier.amqp.states

/**
 * Deletes a queue.
 */
data class DeletedQueue(
    /**
     * Name of the queue.
     */
    val name: String,
    /**
     * If enabled, queue will be deleted only when there are no consumers subscribed to it.
     */
    val ifUnused: Boolean,
    /**
     * If enabled, queue will be deleted only when it's empty.
     */
    val ifEmpty: Boolean,
)
