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

object FrameMethodBasicPublishSerializer : KSerializer<Frame.Method.Basic.Publish> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Publish", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Publish) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.exchange)
        encoder.encodeShortString(value.routingKey)

        var bits = 0u
        if (value.mandatory) bits = bits or (1u shl 0)
        if (value.immediate) bits = bits or (1u shl 1)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Publish {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val exchange = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val mandatory = bits and (1u shl 0) != 0u
        val immediate = bits and (1u shl 1) != 0u

        return Frame.Method.Basic.Publish(
            reserved1 = reserved1,
            exchange = exchange,
            routingKey = routingKey,
            mandatory = mandatory,
            immediate = immediate,
        )
    }

}
