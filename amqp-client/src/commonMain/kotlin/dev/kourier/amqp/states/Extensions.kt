package dev.kourier.amqp.states

/**
 * Binds an exchange using a [BoundExchangeBuilder] block.
 *
 * @param block The block to configure the [BoundExchangeBuilder].
 *
 * @return The constructed [BoundExchange] instance.
 */
fun boundExchange(block: BoundExchangeBuilder.() -> Unit): BoundExchange {
    return BoundExchangeBuilder().apply(block).build()
}

/**
 * Binds a queue using a [BoundQueueBuilder] block.
 *
 * @param block The block to configure the [BoundQueueBuilder].
 *
 * @return The constructed [BoundQueue] instance.
 */
fun boundQueue(block: BoundQueueBuilder.() -> Unit): BoundQueue {
    return BoundQueueBuilder().apply(block).build()
}

/**
 * Declares an exchange using a [DeclaredExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredExchangeBuilder].
 *
 * @return The constructed [DeclaredExchange] instance.
 */
fun declaredExchange(block: DeclaredExchangeBuilder.() -> Unit): DeclaredExchange {
    return DeclaredExchangeBuilder().apply(block).build()
}

/**
 * Declares QoS settings using a [DeclaredQosBuilder] block.
 *
 * @param block The block to configure the [DeclaredQosBuilder].
 *
 * @return The constructed [DeclaredQos] instance.
 */
fun declaredQos(block: DeclaredQosBuilder.() -> Unit): DeclaredQos {
    return DeclaredQosBuilder().apply(block).build()
}

/**
 * Declares a queue using a [DeclaredQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredQueueBuilder].
 *
 * @return The constructed [DeclaredQueue] instance.
 */
fun declaredQueue(block: DeclaredQueueBuilder.() -> Unit): DeclaredQueue {
    return DeclaredQueueBuilder().apply(block).build()
}
