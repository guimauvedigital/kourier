package dev.kourier.amqp

data class AMQPMessage(
    val exchange: String,
    val routingKey: String,
    val deliveryTag: ULong,
    val properties: Properties,
    val redelivered: Boolean,
    val body: ByteArray,
)
