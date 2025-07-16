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
                encoder.encodeSerializableValue(FrameMethodExchangeDeclareSerializer, value.declare)

            is Frame.Method.MethodExchange.DeclareOk -> {}
            is Frame.Method.MethodExchange.Delete ->
                encoder.encodeSerializableValue(FrameMethodExchangeDeleteSerializer, value.delete)

            is Frame.Method.MethodExchange.DeleteOk -> {}
            is Frame.Method.MethodExchange.Bind ->
                encoder.encodeSerializableValue(FrameMethodExchangeBindSerializer, value.bind)

            is Frame.Method.MethodExchange.BindOk -> {}

            is Frame.Method.MethodExchange.Unbind ->
                encoder.encodeSerializableValue(FrameMethodExchangeUnbindSerializer, value.unbind)

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
                Frame.Method.MethodExchange.Declare(decoder.decodeSerializableValue(FrameMethodExchangeDeclareSerializer))

            Frame.Method.MethodExchange.Kind.DECLARE_OK -> Frame.Method.MethodExchange.DeclareOk
            Frame.Method.MethodExchange.Kind.DELETE ->
                Frame.Method.MethodExchange.Delete(decoder.decodeSerializableValue(FrameMethodExchangeDeleteSerializer))

            Frame.Method.MethodExchange.Kind.DELETE_OK -> Frame.Method.MethodExchange.DeleteOk
            Frame.Method.MethodExchange.Kind.BIND ->
                Frame.Method.MethodExchange.Bind(decoder.decodeSerializableValue(FrameMethodExchangeBindSerializer))

            Frame.Method.MethodExchange.Kind.BIND_OK -> Frame.Method.MethodExchange.BindOk
            Frame.Method.MethodExchange.Kind.UNBIND ->
                Frame.Method.MethodExchange.Unbind(decoder.decodeSerializableValue(FrameMethodExchangeUnbindSerializer))

            Frame.Method.MethodExchange.Kind.UNBIND_OK -> Frame.Method.MethodExchange.UnbindOk
        }
    }

}
