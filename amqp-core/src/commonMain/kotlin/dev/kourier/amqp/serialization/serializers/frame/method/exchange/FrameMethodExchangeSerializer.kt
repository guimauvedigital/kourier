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

object FrameMethodExchangeSerializer : KSerializer<Frame.Method.Exchange> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Exchange", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Exchange) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.exchangeKind.value.toShort())
        when (value) {
            is Frame.Method.Exchange.Declare ->
                encoder.encodeSerializableValue(FrameMethodExchangeDeclareSerializer, value)

            is Frame.Method.Exchange.DeclareOk -> {}
            is Frame.Method.Exchange.Delete ->
                encoder.encodeSerializableValue(FrameMethodExchangeDeleteSerializer, value)

            is Frame.Method.Exchange.DeleteOk -> {}
            is Frame.Method.Exchange.Bind -> encoder.encodeSerializableValue(FrameMethodExchangeBindSerializer, value)
            is Frame.Method.Exchange.BindOk -> {}
            is Frame.Method.Exchange.Unbind ->
                encoder.encodeSerializableValue(FrameMethodExchangeUnbindSerializer, value)

            is Frame.Method.Exchange.UnbindOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Exchange {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Exchange.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Exchange.Kind.DECLARE -> decoder.decodeSerializableValue(FrameMethodExchangeDeclareSerializer)
            Frame.Method.Exchange.Kind.DECLARE_OK -> Frame.Method.Exchange.DeclareOk
            Frame.Method.Exchange.Kind.DELETE -> decoder.decodeSerializableValue(FrameMethodExchangeDeleteSerializer)
            Frame.Method.Exchange.Kind.DELETE_OK -> Frame.Method.Exchange.DeleteOk
            Frame.Method.Exchange.Kind.BIND -> decoder.decodeSerializableValue(FrameMethodExchangeBindSerializer)
            Frame.Method.Exchange.Kind.BIND_OK -> Frame.Method.Exchange.BindOk
            Frame.Method.Exchange.Kind.UNBIND -> decoder.decodeSerializableValue(FrameMethodExchangeUnbindSerializer)
            Frame.Method.Exchange.Kind.UNBIND_OK -> Frame.Method.Exchange.UnbindOk
        }
    }

}
