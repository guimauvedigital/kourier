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

object FrameMethodChannelOpenSerializer : KSerializer<Frame.Method.Channel.Open> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Channel.Open", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Channel.Open) {
        require(encoder is ProtocolBinaryEncoder)
        encoder.encodeShortString(value.reserved1)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Channel.Open {
        require(decoder is ProtocolBinaryDecoder)
        val reserved1 = decoder.decodeShortString().first
        return Frame.Method.Channel.Open(reserved1)
    }

}
