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

object FrameMethodBasicGetEmptySerializer : KSerializer<Frame.Method.Basic.GetEmpty> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.GetEmpty", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.GetEmpty) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.reserved1)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.GetEmpty {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShortString().first

        return Frame.Method.Basic.GetEmpty(
            reserved1 = reserved1,
        )
    }

}
