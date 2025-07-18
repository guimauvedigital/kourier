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

object FrameMethodQueueUnbindSerializer : KSerializer<Frame.Method.MethodQueue.Unbind> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodQueue.Unbind", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodQueue.Unbind) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.queueName)
        encoder.encodeShortString(value.exchangeName)
        encoder.encodeShortString(value.routingKey)
        encoder.encodeSerializableValue(TableSerializer, value.arguments)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodQueue.Unbind {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val queueName = decoder.decodeShortString().first
        val exchangeName = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first
        val arguments = decoder.decodeSerializableValue(TableSerializer)

        return Frame.Method.MethodQueue.Unbind(
            reserved1 = reserved1,
            queueName = queueName,
            exchangeName = exchangeName,
            routingKey = routingKey,
            arguments = arguments
        )
    }

}
