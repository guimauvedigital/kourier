package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPMessage
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Frame
import dev.kourier.amqp.Properties

data class PartialDelivery(
    val method: Frame.Method.Basic,
) {

    private var header: Frame.Header? = null
    private var payload: ByteArray? = null

    val isComplete: Boolean
        get() = header != null && header!!.bodySize <= (payload?.size ?: 0).toUInt()

    // NOTE: should be made throwing with validation for a more restrictive protocol implementation
    fun setHeader(header: Frame.Header) {
        // validate that self.header == null
        if (this.header != null) {
            // Optionally throw or handle error
            return
        }
        this.header = header
    }

    // NOTE: should be made throwing with validation for a more restrictive protocol implementation
    fun addBody(buffer: ByteArray) {
        val header = this.header ?: return // probably should take channel down

        if (payload == null) {
            // Reserve capacity (not needed for ByteArray, but can preallocate)
            payload = buffer.copyOf()
        } else {
            val oldPayload = payload!!
            val newPayload = ByteArray(oldPayload.size + buffer.size)
            oldPayload.copyInto(newPayload, 0, 0, oldPayload.size)
            buffer.copyInto(newPayload, oldPayload.size, 0, buffer.size)
            payload = newPayload
        }
    }

    fun asCompletedMessage(): Triple<Frame.Method.Basic, Properties, ByteArray> {
        // NOTE: this could be made a consuming func once partial is possible I think
        check(isComplete)

        // header and payloads are guaranteed to be non-null after isComplete
        return Triple(method, header!!.properties, payload ?: ByteArray(0))
    }

    suspend fun emitOnChannel(channel: DefaultAMQPChannel) {
        val (method, properties, completeBody) = asCompletedMessage()
        channel.nextMessage = null

        when (method) {
            is Frame.Method.Basic.GetOk -> channel.channelResponses.emit(
                AMQPResponse.Channel.Message.Get(
                    message = AMQPMessage(
                        exchange = method.exchange,
                        routingKey = method.routingKey,
                        deliveryTag = method.deliveryTag,
                        properties = properties,
                        redelivered = method.redelivered,
                        body = completeBody
                    ),
                    messageCount = method.messageCount
                )
            )

            is Frame.Method.Basic.Deliver -> channel.channelResponses.emit(
                AMQPResponse.Channel.Message.Delivery(
                    message = AMQPMessage(
                        exchange = method.exchange,
                        routingKey = method.routingKey,
                        deliveryTag = method.deliveryTag,
                        properties = properties,
                        redelivered = method.redelivered,
                        body = completeBody
                    ),
                    consumerTag = method.consumerTag
                ),
            )

            is Frame.Method.Basic.Return -> channel.channelResponses.emit(
                AMQPResponse.Channel.Message.Return(
                    replyCode = method.replyCode,
                    replyText = method.replyText,
                    exchange = method.exchange,
                    routingKey = method.routingKey,
                    properties = properties,
                    body = completeBody
                )
            )

            else -> error("Unexpected frame: $method")
        }
    }

}
