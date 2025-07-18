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

object FrameMethodQueuePurgeSerializer : KSerializer<Frame.Method.Queue.Purge> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Queue.Purge", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Queue.Purge) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.queueName)

        var bits = 0u
        if (value.noWait) bits = bits or (1u shl 0)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Queue.Purge {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val queueName = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val noWait = bits and (1u shl 0) != 0u

        return Frame.Method.Queue.Purge(
            reserved1 = reserved1,
            queueName = queueName,
            noWait = noWait,
        )
    }

}
