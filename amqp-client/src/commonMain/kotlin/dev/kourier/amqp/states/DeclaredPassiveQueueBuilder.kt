package dev.kourier.amqp.states

/**
 * Passively declares a queue.
 */
class DeclaredPassiveQueueBuilder {

    /**
     * Name of the queue.
     */
    var name: String = ""

    /**
     * Builds the [DeclaredPassiveQueue] instance.
     *
     * @return The constructed [DeclaredPassiveQueue].
     */
    fun build(): DeclaredPassiveQueue {
        return DeclaredPassiveQueue(
            name = name,
        )
    }

}
