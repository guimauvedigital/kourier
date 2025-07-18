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

object FrameMethodBasicConsumeOkSerializer : KSerializer<Frame.Method.Basic.ConsumeOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.ConsumeOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.ConsumeOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.consumerTag)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.ConsumeOk {
        require(decoder is ProtocolBinaryDecoder)

        val consumerTag = decoder.decodeShortString().first

        return Frame.Method.Basic.ConsumeOk(
            consumerTag = consumerTag,
        )
    }

}
