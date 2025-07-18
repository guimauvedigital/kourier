package dev.kourier.amqp.serialization.serializers.frame.method.channel

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

object FrameMethodChannelCloseSerializer : KSerializer<Frame.Method.MethodChannel.Close> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodChannel.Close", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodChannel.Close) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.replyCode.toShort())
        encoder.encodeShortString(value.replyText)
        encoder.encodeShort(value.classId.toShort())
        encoder.encodeShort(value.methodId.toShort())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodChannel.Close {
        require(decoder is ProtocolBinaryDecoder)

        val replyCode = decoder.decodeShort().toUShort()
        val replyText = decoder.decodeShortString().first
        val classId = decoder.decodeShort().toUShort()
        val methodId = decoder.decodeShort().toUShort()

        return Frame.Method.MethodChannel.Close(
            replyCode = replyCode,
            replyText = replyText,
            classId = classId,
            methodId = methodId
        )
    }

}
