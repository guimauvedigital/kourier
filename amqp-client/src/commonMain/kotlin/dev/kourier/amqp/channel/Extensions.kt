package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.states.*

/**
 * Declares an exchange using a [DeclaredExchange] instance.
 *
 * @param declaredExchange The [DeclaredExchange] instance containing the exchange properties.
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclare(declaredExchange: DeclaredExchange): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclare(
        name = declaredExchange.name,
        type = declaredExchange.type,
        durable = declaredExchange.durable,
        autoDelete = declaredExchange.autoDelete,
        internal = declaredExchange.internal,
        arguments = declaredExchange.arguments
    )
}

/**
 * Declares an exchange using a [DeclaredExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredExchangeBuilder].
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclare(block: DeclaredExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclare(declaredExchange(block))
}

/**
 * Declares QoS settings using a [DeclaredQos] instance.
 *
 * @param declaredQos The [DeclaredQos] instance containing the QoS properties.
 *
 * @return The QoS declaration response.
 */
suspend fun AMQPChannel.basicQos(declaredQos: DeclaredQos): AMQPResponse.Channel.Basic.QosOk {
    return this.basicQos(
        count = declaredQos.count,
        global = declaredQos.global
    )
}

/**
 * Declares QoS settings using a [DeclaredQosBuilder] block.
 *
 * @param block The block to configure the [DeclaredQosBuilder].
 *
 * @return The QoS declaration response.
 */
suspend fun AMQPChannel.basicQos(block: DeclaredQosBuilder.() -> Unit): AMQPResponse.Channel.Basic.QosOk {
    return this.basicQos(declaredQos(block))
}

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
