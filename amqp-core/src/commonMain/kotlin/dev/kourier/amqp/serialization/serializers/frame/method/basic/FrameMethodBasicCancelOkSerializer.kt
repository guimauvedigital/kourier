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

object FrameMethodBasicCancelOkSerializer : KSerializer<Frame.Method.Basic.CancelOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.CancelOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.CancelOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.consumerTag)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.CancelOk {
        require(decoder is ProtocolBinaryDecoder)

        val consumerTag = decoder.decodeShortString().first

        return Frame.Method.Basic.CancelOk(
            consumerTag = consumerTag,
        )
    }

}
