package dev.kourier.amqp.states

import dev.kourier.amqp.Table

data class DeclaredQueue(
    val name: String,
    val durable: Boolean,
    val exclusive: Boolean,
    val autoDelete: Boolean,
    val arguments: Table,
)
