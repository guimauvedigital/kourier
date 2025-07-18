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

object FrameMethodChannelSerializer : KSerializer<Frame.Method.Channel> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Channel", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Channel) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.channelKind.value.toShort())
        when (value) {
            is Frame.Method.Channel.Open -> encoder.encodeShortString(value.reserved1)
            is Frame.Method.Channel.OpenOk -> encoder.encodeLongString(value.reserved1)
            is Frame.Method.Channel.Flow -> encoder.encodeBoolean(value.active)
            is Frame.Method.Channel.FlowOk -> encoder.encodeBoolean(value.active)
            is Frame.Method.Channel.Close -> encoder.encodeSerializableValue(FrameMethodChannelCloseSerializer, value)
            is Frame.Method.Channel.CloseOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Channel {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Channel.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Channel.Kind.OPEN -> {
                val reserved1 = decoder.decodeShortString().first
                Frame.Method.Channel.Open(reserved1)
            }

            Frame.Method.Channel.Kind.OPEN_OK -> {
                val reserved1 = decoder.decodeLongString().first
                Frame.Method.Channel.OpenOk(reserved1)
            }

            Frame.Method.Channel.Kind.FLOW -> {
                val active = decoder.decodeBoolean()
                Frame.Method.Channel.Flow(active)
            }

            Frame.Method.Channel.Kind.FLOW_OK -> {
                val active = decoder.decodeBoolean()
                Frame.Method.Channel.FlowOk(active)
            }

            Frame.Method.Channel.Kind.CLOSE -> decoder.decodeSerializableValue(FrameMethodChannelCloseSerializer)
            Frame.Method.Channel.Kind.CLOSE_OK -> Frame.Method.Channel.CloseOk
        }
    }

}
