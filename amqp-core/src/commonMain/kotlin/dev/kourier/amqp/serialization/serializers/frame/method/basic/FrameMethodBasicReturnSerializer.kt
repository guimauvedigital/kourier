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

object FrameMethodBasicReturnSerializer : KSerializer<Frame.Method.Basic.Return> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Return", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Return) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.replyCode.toShort())
        encoder.encodeShortString(value.replyText)
        encoder.encodeShortString(value.exchange)
        encoder.encodeShortString(value.routingKey)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Return {
        require(decoder is ProtocolBinaryDecoder)

        val replyCode = decoder.decodeShort().toUShort()
        val replyText = decoder.decodeShortString().first
        val exchange = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first

        return Frame.Method.Basic.Return(
            replyCode = replyCode,
            replyText = replyText,
            exchange = exchange,
            routingKey = routingKey,
        )
    }

}
