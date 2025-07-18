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

object FrameMethodBasicSerializer : KSerializer<Frame.Method.Basic> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.basicKind.value.toShort())
        when (value) {
            is Frame.Method.Basic.Qos -> encoder.encodeSerializableValue(FrameMethodBasicQosSerializer, value)
            is Frame.Method.Basic.QosOk -> {}
            is Frame.Method.Basic.Consume -> encoder.encodeSerializableValue(FrameMethodBasicConsumeSerializer, value)
            is Frame.Method.Basic.ConsumeOk ->
                encoder.encodeSerializableValue(FrameMethodBasicConsumeOkSerializer, value)
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Basic.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Basic.Kind.QOS -> decoder.decodeSerializableValue(FrameMethodBasicQosSerializer)
            Frame.Method.Basic.Kind.QOS_OK -> Frame.Method.Basic.QosOk
            Frame.Method.Basic.Kind.CONSUME -> decoder.decodeSerializableValue(FrameMethodBasicConsumeSerializer)
            Frame.Method.Basic.Kind.CONSUME_OK -> decoder.decodeSerializableValue(FrameMethodBasicConsumeOkSerializer)
            Frame.Method.Basic.Kind.CANCEL -> TODO()
            Frame.Method.Basic.Kind.CANCEL_OK -> TODO()
            Frame.Method.Basic.Kind.PUBLISH -> TODO()
            Frame.Method.Basic.Kind.RETURN -> TODO()
            Frame.Method.Basic.Kind.DELIVER -> TODO()
            Frame.Method.Basic.Kind.GET -> TODO()
            Frame.Method.Basic.Kind.GET_OK -> TODO()
            Frame.Method.Basic.Kind.GET_EMPTY -> TODO()
            Frame.Method.Basic.Kind.ACK -> TODO()
            Frame.Method.Basic.Kind.REJECT -> TODO()
            Frame.Method.Basic.Kind.RECOVER_ASYNC -> TODO()
            Frame.Method.Basic.Kind.RECOVER -> TODO()
            Frame.Method.Basic.Kind.RECOVER_OK -> TODO()
            Frame.Method.Basic.Kind.NACK -> TODO()
        }
    }

}
