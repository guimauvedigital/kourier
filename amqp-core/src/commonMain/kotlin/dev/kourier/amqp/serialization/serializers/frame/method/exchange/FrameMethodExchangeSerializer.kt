package dev.kourier.amqp.serialization.serializers.frame.method.exchange

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

object FrameMethodExchangeSerializer : KSerializer<Frame.Method.MethodExchange> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodExchange", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodExchange) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.kind.value.toShort())
        when (value) {
            is Frame.Method.MethodExchange.Declare ->
                encoder.encodeSerializableValue(FrameMethodExchangeDeclareSerializer, value)

            is Frame.Method.MethodExchange.DeclareOk -> {}
            is Frame.Method.MethodExchange.Delete ->
                encoder.encodeSerializableValue(FrameMethodExchangeDeleteSerializer, value)

            is Frame.Method.MethodExchange.DeleteOk -> {}
            is Frame.Method.MethodExchange.Bind ->
                encoder.encodeSerializableValue(FrameMethodExchangeBindSerializer, value)

            is Frame.Method.MethodExchange.BindOk -> {}

            is Frame.Method.MethodExchange.Unbind ->
                encoder.encodeSerializableValue(FrameMethodExchangeUnbindSerializer, value)

            is Frame.Method.MethodExchange.UnbindOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodExchange {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.MethodExchange.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.MethodExchange.Kind.DECLARE ->
                decoder.decodeSerializableValue(FrameMethodExchangeDeclareSerializer)

            Frame.Method.MethodExchange.Kind.DECLARE_OK -> Frame.Method.MethodExchange.DeclareOk
            Frame.Method.MethodExchange.Kind.DELETE ->
                decoder.decodeSerializableValue(FrameMethodExchangeDeleteSerializer)

            Frame.Method.MethodExchange.Kind.DELETE_OK -> Frame.Method.MethodExchange.DeleteOk
            Frame.Method.MethodExchange.Kind.BIND -> decoder.decodeSerializableValue(FrameMethodExchangeBindSerializer)

            Frame.Method.MethodExchange.Kind.BIND_OK -> Frame.Method.MethodExchange.BindOk
            Frame.Method.MethodExchange.Kind.UNBIND ->
                decoder.decodeSerializableValue(FrameMethodExchangeUnbindSerializer)

            Frame.Method.MethodExchange.Kind.UNBIND_OK -> Frame.Method.MethodExchange.UnbindOk
        }
    }

}
