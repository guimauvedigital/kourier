package dev.kourier.amqp.robust.states

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Table

data class ConsumedQueue(
    val queue: String,
    val consumerTag: String,
    val noAck: Boolean,
    val exclusive: Boolean,
    val arguments: Table,
    val onDelivery: suspend (AMQPResponse.Channel.Message.Delivery) -> Unit,
    val onCanceled: suspend (AMQPResponse.Channel) -> Unit,
)
