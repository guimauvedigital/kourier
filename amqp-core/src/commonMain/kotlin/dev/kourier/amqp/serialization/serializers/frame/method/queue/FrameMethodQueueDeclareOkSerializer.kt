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

object FrameMethodQueueDeclareOkSerializer : KSerializer<Frame.Method.Queue.DeclareOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Queue.DeclareOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Queue.DeclareOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.queueName)
        encoder.encodeInt(value.messageCount.toInt())
        encoder.encodeInt(value.consumerCount.toInt())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Queue.DeclareOk {
        require(decoder is ProtocolBinaryDecoder)

        val queueName = decoder.decodeShortString().first
        val messageCount = decoder.decodeInt().toUInt()
        val consumerCount = decoder.decodeInt().toUInt()

        return Frame.Method.Queue.DeclareOk(
            queueName = queueName,
            messageCount = messageCount,
            consumerCount = consumerCount
        )
    }

}
