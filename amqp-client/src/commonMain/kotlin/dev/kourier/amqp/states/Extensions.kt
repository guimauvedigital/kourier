package dev.kourier.amqp.states

fun declaredQueue(block: DeclaredQueueBuilder.() -> Unit): DeclaredQueue {
    return DeclaredQueueBuilder().apply(block).build()
}
