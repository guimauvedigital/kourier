package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import dev.kourier.amqp.serialization.serializers.TableSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicConsumeSerializer : KSerializer<Frame.Method.Basic.Consume> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Consume", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Consume) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.queue)
        encoder.encodeShortString(value.consumerTag)

        var bits = 0u
        if (value.noLocal) bits = bits or (1u shl 0)
        if (value.noAck) bits = bits or (1u shl 1)
        if (value.exclusive) bits = bits or (1u shl 2)
        if (value.noWait) bits = bits or (1u shl 3)

        encoder.encodeByte(bits.toByte())
        encoder.encodeSerializableValue(TableSerializer, value.arguments)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Consume {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val queue = decoder.decodeShortString().first
        val consumerTag = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val noLocal = bits and (1u shl 0) != 0u
        val noAck = bits and (1u shl 1) != 0u
        val exclusive = bits and (1u shl 2) != 0u
        val noWait = bits and (1u shl 3) != 0u

        val arguments = decoder.decodeSerializableValue(TableSerializer)

        return Frame.Method.Basic.Consume(
            reserved1 = reserved1,
            queue = queue,
            consumerTag = consumerTag,
            noLocal = noLocal,
            noAck = noAck,
            exclusive = exclusive,
            noWait = noWait,
            arguments = arguments
        )
    }

}
