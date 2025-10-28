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

object FrameMethodChannelOpenOkSerializer : KSerializer<Frame.Method.Channel.OpenOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Channel.OpenOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Channel.OpenOk) {
        require(encoder is ProtocolBinaryEncoder)
        encoder.encodeLongString(value.reserved1)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Channel.OpenOk {
        require(decoder is ProtocolBinaryDecoder)
        val reserved1 = decoder.decodeLongString().first
        return Frame.Method.Channel.OpenOk(reserved1)
    }

}
