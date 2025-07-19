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

object FrameMethodBasicCancelSerializer : KSerializer<Frame.Method.Basic.Cancel> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Cancel", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Cancel) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.consumerTag)

        var bits = 0u
        if (value.noWait) bits = bits or (1u shl 0)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Cancel {
        require(decoder is ProtocolBinaryDecoder)

        val consumerTag = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val noWait = bits and (1u shl 0) != 0u

        return Frame.Method.Basic.Cancel(
            consumerTag = consumerTag,
            noWait = noWait,
        )
    }

}
