package dev.kourier.amqp.serialization.serializers.frame.method.channel

import dev.kourier.amqp.Frame
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
        encoder.encodeShort(value.channelKind.value.toShort())
        when (value) {
            is Frame.Method.Channel.Open -> encoder.encodeSerializableValue(FrameMethodChannelOpenSerializer, value)
            is Frame.Method.Channel.OpenOk -> encoder.encodeSerializableValue(FrameMethodChannelOpenOkSerializer, value)
            is Frame.Method.Channel.Flow -> encoder.encodeSerializableValue(FrameMethodChannelFlowSerializer, value)
            is Frame.Method.Channel.FlowOk -> encoder.encodeSerializableValue(FrameMethodChannelFlowOkSerializer, value)
            is Frame.Method.Channel.Close -> encoder.encodeSerializableValue(FrameMethodChannelCloseSerializer, value)
            is Frame.Method.Channel.CloseOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Channel {
        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Channel.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Channel.Kind.OPEN -> decoder.decodeSerializableValue(FrameMethodChannelOpenSerializer)
            Frame.Method.Channel.Kind.OPEN_OK -> decoder.decodeSerializableValue(FrameMethodChannelOpenOkSerializer)
            Frame.Method.Channel.Kind.FLOW -> decoder.decodeSerializableValue(FrameMethodChannelFlowSerializer)
            Frame.Method.Channel.Kind.FLOW_OK -> decoder.decodeSerializableValue(FrameMethodChannelFlowOkSerializer)
            Frame.Method.Channel.Kind.CLOSE -> decoder.decodeSerializableValue(FrameMethodChannelCloseSerializer)
            Frame.Method.Channel.Kind.CLOSE_OK -> Frame.Method.Channel.CloseOk
        }
    }

}
