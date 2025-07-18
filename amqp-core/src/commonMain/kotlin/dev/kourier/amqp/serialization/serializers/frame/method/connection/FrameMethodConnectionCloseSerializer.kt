package dev.kourier.amqp.serialization.serializers.frame.method.connection

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

object FrameMethodConnectionCloseSerializer : KSerializer<Frame.Method.Connection.Close> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.Close", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.Close) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.replyCode.toShort())
        encoder.encodeShortString(value.replyText)
        encoder.encodeShort(value.failingClassId.toShort())
        encoder.encodeShort(value.failingMethodId.toShort())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.Close {
        require(decoder is ProtocolBinaryDecoder)

        val replyCode = decoder.decodeShort().toUShort()
        val replyText = decoder.decodeShortString().first
        val failingClassId = decoder.decodeShort().toUShort()
        val failingMethodId = decoder.decodeShort().toUShort()

        return Frame.Method.Connection.Close(
            replyCode = replyCode,
            replyText = replyText,
            failingClassId = failingClassId,
            failingMethodId = failingMethodId
        )
    }

}
