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

object FrameMethodBasicGetOkSerializer : KSerializer<Frame.Method.Basic.GetOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.GetOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.GetOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeLong(value.deliveryTag.toLong())
        encoder.encodeBoolean(value.redelivered)
        encoder.encodeShortString(value.exchange)
        encoder.encodeShortString(value.routingKey)
        encoder.encodeInt(value.messageCount.toInt())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.GetOk {
        require(decoder is ProtocolBinaryDecoder)

        val deliveryTag = decoder.decodeLong().toULong()
        val redelivered = decoder.decodeBoolean()
        val exchange = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first
        val messageCount = decoder.decodeInt().toUInt()

        return Frame.Method.Basic.GetOk(
            deliveryTag = deliveryTag,
            redelivered = redelivered,
            exchange = exchange,
            routingKey = routingKey,
            messageCount = messageCount,
        )
    }

}
