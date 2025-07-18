package dev.kourier.amqp.serialization.serializers.frame.method.queue

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

object FrameMethodQueueDeleteSerializer : KSerializer<Frame.Method.Queue.Delete> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Queue.Delete", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Queue.Delete) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.queueName)

        var bits = 0u
        if (value.ifUnused) bits = bits or (1u shl 0)
        if (value.ifEmpty) bits = bits or (1u shl 1)
        if (value.noWait) bits = bits or (1u shl 2)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Queue.Delete {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val queueName = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val ifUnused = bits and (1u shl 0) != 0u
        val ifEmpty = bits and (1u shl 1) != 0u
        val noWait = bits and (1u shl 2) != 0u

        return Frame.Method.Queue.Delete(
            reserved1 = reserved1,
            queueName = queueName,
            ifUnused = ifUnused,
            ifEmpty = ifEmpty,
            noWait = noWait,
        )
    }

}
