package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPMessage
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Frame
import dev.kourier.amqp.Properties
import io.ktor.util.logging.*

data class PartialDelivery(
    val method: Frame.Method.Basic,
) {

    private val logger = KtorSimpleLogger("PartialDelivery")

    private var header: Frame.Header? = null
    private var payload: ByteArray? = null

    val isComplete: Boolean
        get() = header != null && header!!.bodySize <= (payload?.size ?: 0).toUInt()

    fun setHeader(header: Frame.Header) {
        if (this.header != null) error("Header already set")
        logger.debug("Setting PartialDelivery header: $header")
        this.header = header
    }

    // NOTE: should be made throwing with validation for a more restrictive protocol implementation
    fun addBody(buffer: ByteArray) {
        if (this.header == null) error("Header must be set before adding body")

        if (payload == null) {
            logger.debug("Setting initial PartialDelivery body payload of size ${buffer.size} bytes")
            // Reserve capacity (not needed for ByteArray, but can preallocate)
            payload = buffer.copyOf()
        } else {
            logger.debug("Appending to PartialDelivery body payload with additional size ${buffer.size} bytes")
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
        logger.debug("Emitting completed PartialDelivery on channel ${channel.id}")
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
