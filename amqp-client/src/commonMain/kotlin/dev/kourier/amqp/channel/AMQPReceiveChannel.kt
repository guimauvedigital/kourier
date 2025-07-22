package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPResponse
import kotlinx.coroutines.channels.ReceiveChannel

class AMQPReceiveChannel(
    val consumeOk: AMQPResponse.Channel.Basic.ConsumeOk,
    val receiveChannel: ReceiveChannel<AMQPResponse.Channel.Message.Delivery>,
) : ReceiveChannel<AMQPResponse.Channel.Message.Delivery> by receiveChannel
