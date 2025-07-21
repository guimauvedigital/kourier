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

            is Frame.Method.Basic.Cancel -> encoder.encodeSerializableValue(FrameMethodBasicCancelSerializer, value)
            is Frame.Method.Basic.CancelOk -> encoder.encodeSerializableValue(FrameMethodBasicCancelOkSerializer, value)
            is Frame.Method.Basic.Publish -> encoder.encodeSerializableValue(FrameMethodBasicPublishSerializer, value)
            is Frame.Method.Basic.Return -> encoder.encodeSerializableValue(FrameMethodBasicReturnSerializer, value)
            is Frame.Method.Basic.Deliver -> encoder.encodeSerializableValue(FrameMethodBasicDeliverSerializer, value)
            is Frame.Method.Basic.Get -> encoder.encodeSerializableValue(FrameMethodBasicGetSerializer, value)
            is Frame.Method.Basic.GetOk -> encoder.encodeSerializableValue(FrameMethodBasicGetOkSerializer, value)
            is Frame.Method.Basic.GetEmpty -> encoder.encodeSerializableValue(FrameMethodBasicGetEmptySerializer, value)
            is Frame.Method.Basic.Ack -> encoder.encodeSerializableValue(FrameMethodBasicAckSerializer, value)
            is Frame.Method.Basic.Reject -> encoder.encodeSerializableValue(FrameMethodBasicRejectSerializer, value)

            is Frame.Method.Basic.Nack -> encoder.encodeSerializableValue(FrameMethodBasicNackSerializer, value)
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
            Frame.Method.Basic.Kind.CANCEL -> decoder.decodeSerializableValue(FrameMethodBasicCancelSerializer)
            Frame.Method.Basic.Kind.CANCEL_OK -> decoder.decodeSerializableValue(FrameMethodBasicCancelOkSerializer)
            Frame.Method.Basic.Kind.PUBLISH -> decoder.decodeSerializableValue(FrameMethodBasicPublishSerializer)
            Frame.Method.Basic.Kind.RETURN -> decoder.decodeSerializableValue(FrameMethodBasicReturnSerializer)
            Frame.Method.Basic.Kind.DELIVER -> decoder.decodeSerializableValue(FrameMethodBasicDeliverSerializer)
            Frame.Method.Basic.Kind.GET -> decoder.decodeSerializableValue(FrameMethodBasicGetSerializer)
            Frame.Method.Basic.Kind.GET_OK -> decoder.decodeSerializableValue(FrameMethodBasicGetOkSerializer)
            Frame.Method.Basic.Kind.GET_EMPTY -> decoder.decodeSerializableValue(FrameMethodBasicGetEmptySerializer)
            Frame.Method.Basic.Kind.ACK -> decoder.decodeSerializableValue(FrameMethodBasicAckSerializer)
            Frame.Method.Basic.Kind.REJECT -> decoder.decodeSerializableValue(FrameMethodBasicRejectSerializer)
            Frame.Method.Basic.Kind.RECOVER_ASYNC -> TODO()
            Frame.Method.Basic.Kind.RECOVER -> TODO()
            Frame.Method.Basic.Kind.RECOVER_OK -> TODO()
            Frame.Method.Basic.Kind.NACK -> decoder.decodeSerializableValue(FrameMethodBasicNackSerializer)
        }
    }

}
