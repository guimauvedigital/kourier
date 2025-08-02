package dev.kourier.amqp.robust.states

import dev.kourier.amqp.Table

data class BoundQueue(
    val queue: String,
    val exchange: String,
    val routingKey: String,
    val arguments: Table,
)
