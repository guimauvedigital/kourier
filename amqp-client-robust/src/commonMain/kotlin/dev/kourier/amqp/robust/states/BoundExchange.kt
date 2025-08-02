package dev.kourier.amqp.robust.states

import dev.kourier.amqp.Table

data class BoundExchange(
    val destination: String,
    val source: String,
    val routingKey: String,
    val arguments: Table,
)
