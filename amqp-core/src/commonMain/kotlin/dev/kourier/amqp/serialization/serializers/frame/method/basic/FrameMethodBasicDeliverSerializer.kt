package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicDeliverSerializer : KSerializer<Frame.Method.Basic.Deliver> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Deliver", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Deliver) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.consumerTag)
        encoder.encodeLong(value.deliveryTag.toLong())
        encoder.encodeBoolean(value.redelivered)
        encoder.encodeShortString(value.exchange)
        encoder.encodeShortString(value.routingKey)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Deliver {
        require(decoder is ProtocolBinaryDecoder)

        val consumerTag = decoder.decodeShortString().first
        val deliveryTag = decoder.decodeLong().toULong()
        val redelivered = decoder.decodeBoolean()
        val exchange = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first

        return Frame.Method.Basic.Deliver(
            consumerTag = consumerTag,
            deliveryTag = deliveryTag,
            redelivered = redelivered,
            exchange = exchange,
            routingKey = routingKey,
        )
    }

}
