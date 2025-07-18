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

object FrameMethodChannelSerializer : KSerializer<Frame.Method.MethodChannel> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodChannel", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodChannel) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.kind.value.toShort())
        when (value) {
            is Frame.Method.MethodChannel.Open -> encoder.encodeShortString(value.reserved1)
            is Frame.Method.MethodChannel.OpenOk -> encoder.encodeLongString(value.reserved1)
            is Frame.Method.MethodChannel.Flow -> encoder.encodeBoolean(value.active)
            is Frame.Method.MethodChannel.FlowOk -> encoder.encodeBoolean(value.active)
            is Frame.Method.MethodChannel.Close ->
                encoder.encodeSerializableValue(FrameMethodChannelCloseSerializer, value)

            is Frame.Method.MethodChannel.CloseOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodChannel {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.MethodChannel.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.MethodChannel.Kind.OPEN -> {
                val reserved1 = decoder.decodeShortString().first
                Frame.Method.MethodChannel.Open(reserved1)
            }

            Frame.Method.MethodChannel.Kind.OPEN_OK -> {
                val reserved1 = decoder.decodeLongString().first
                Frame.Method.MethodChannel.OpenOk(reserved1)
            }

            Frame.Method.MethodChannel.Kind.FLOW -> {
                val active = decoder.decodeBoolean()
                Frame.Method.MethodChannel.Flow(active)
            }

            Frame.Method.MethodChannel.Kind.FLOW_OK -> {
                val active = decoder.decodeBoolean()
                Frame.Method.MethodChannel.FlowOk(active)
            }

            Frame.Method.MethodChannel.Kind.CLOSE -> decoder.decodeSerializableValue(FrameMethodChannelCloseSerializer)
            Frame.Method.MethodChannel.Kind.CLOSE_OK -> Frame.Method.MethodChannel.CloseOk
        }
    }

}
