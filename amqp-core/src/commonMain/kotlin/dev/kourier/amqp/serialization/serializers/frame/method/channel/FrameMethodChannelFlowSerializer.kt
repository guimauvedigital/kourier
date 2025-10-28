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

object FrameMethodChannelFlowSerializer : KSerializer<Frame.Method.Channel.Flow> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Channel.Flow", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Channel.Flow) {
        require(encoder is ProtocolBinaryEncoder)
        encoder.encodeBoolean(value.active)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Channel.Flow {
        require(decoder is ProtocolBinaryDecoder)
        val active = decoder.decodeBoolean()
        return Frame.Method.Channel.Flow(active)
    }

}
