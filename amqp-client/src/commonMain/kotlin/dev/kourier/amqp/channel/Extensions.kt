package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.states.DeclaredQueue
import dev.kourier.amqp.states.DeclaredQueueBuilder
import dev.kourier.amqp.states.declaredQueue

/**
 * Declares a queue using a [DeclaredQueue] instance.
 *
 * @param declaredQueue The [DeclaredQueue] instance containing the queue properties.
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclare(declaredQueue: DeclaredQueue): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclare(
        name = declaredQueue.name,
        durable = declaredQueue.durable,
        exclusive = declaredQueue.exclusive,
        autoDelete = declaredQueue.autoDelete,
        arguments = declaredQueue.arguments
    )
}

/**
 * Declares a queue using a [DeclaredQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredQueueBuilder].
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclare(block: DeclaredQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclare(declaredQueue(block))
}
