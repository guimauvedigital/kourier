package dev.kourier.amqp.robust.states

import dev.kourier.amqp.Table

data class DeclaredExchange(
    val name: String,
    val type: String,
    val durable: Boolean,
    val autoDelete: Boolean,
    val internal: Boolean,
    val arguments: Table,
)
