package dev.kourier.amqp.serialization.serializers.frame.method.queue

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

object FrameMethodQueueDeclareSerializer : KSerializer<Frame.Method.MethodQueue.QueueDeclare> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodQueue.QueueDeclare", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodQueue.QueueDeclare) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.queueName)

        var bits = 0u
        if (value.passive) bits = bits or (1u shl 0)
        if (value.durable) bits = bits or (1u shl 1)
        if (value.exclusive) bits = bits or (1u shl 2)
        if (value.autoDelete) bits = bits or (1u shl 3)
        if (value.noWait) bits = bits or (1u shl 4)

        encoder.encodeByte(bits.toByte())
        encoder.encodeSerializableValue(TableSerializer, value.arguments)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodQueue.QueueDeclare {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val queueName = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val passive = bits and (1u shl 0) != 0u
        val durable = bits and (1u shl 1) != 0u
        val exclusive = bits and (1u shl 2) != 0u
        val autoDelete = bits and (1u shl 3) != 0u
        val noWait = bits and (1u shl 4) != 0u

        val arguments = decoder.decodeSerializableValue(TableSerializer)

        return Frame.Method.MethodQueue.QueueDeclare(
            reserved1 = reserved1,
            queueName = queueName,
            passive = passive,
            durable = durable,
            exclusive = exclusive,
            autoDelete = autoDelete,
            noWait = noWait,
            arguments = arguments
        )
    }

}
