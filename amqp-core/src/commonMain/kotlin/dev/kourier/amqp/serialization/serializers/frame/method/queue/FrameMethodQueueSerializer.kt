package dev.kourier.amqp.serialization.serializers.frame.method.queue

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

object FrameMethodQueueSerializer : KSerializer<Frame.Method.Queue> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Queue", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Queue) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.queueKind.value.toShort())
        when (value) {
            is Frame.Method.Queue.Declare -> encoder.encodeSerializableValue(FrameMethodQueueDeclareSerializer, value)
            is Frame.Method.Queue.DeclareOk ->
                encoder.encodeSerializableValue(FrameMethodQueueDeclareOkSerializer, value)

            is Frame.Method.Queue.Bind -> encoder.encodeSerializableValue(FrameMethodQueueBindSerializer, value)
            is Frame.Method.Queue.BindOk -> {}
            is Frame.Method.Queue.Purge -> encoder.encodeSerializableValue(FrameMethodQueuePurgeSerializer, value)
            is Frame.Method.Queue.PurgeOk -> encoder.encodeSerializableValue(FrameMethodQueuePurgeOkSerializer, value)
            is Frame.Method.Queue.Delete -> encoder.encodeSerializableValue(FrameMethodQueueDeleteSerializer, value)
            is Frame.Method.Queue.DeleteOk -> encoder.encodeSerializableValue(FrameMethodQueueDeleteOkSerializer, value)
            is Frame.Method.Queue.Unbind -> encoder.encodeSerializableValue(FrameMethodQueueUnbindSerializer, value)
            is Frame.Method.Queue.UnbindOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Queue {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Queue.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Queue.Kind.DECLARE -> decoder.decodeSerializableValue(FrameMethodQueueDeclareSerializer)
            Frame.Method.Queue.Kind.DECLARE_OK -> decoder.decodeSerializableValue(FrameMethodQueueDeclareOkSerializer)
            Frame.Method.Queue.Kind.BIND -> decoder.decodeSerializableValue(FrameMethodQueueBindSerializer)
            Frame.Method.Queue.Kind.BIND_OK -> Frame.Method.Queue.BindOk
            Frame.Method.Queue.Kind.PURGE -> decoder.decodeSerializableValue(FrameMethodQueuePurgeSerializer)
            Frame.Method.Queue.Kind.PURGE_OK -> decoder.decodeSerializableValue(FrameMethodQueuePurgeOkSerializer)
            Frame.Method.Queue.Kind.DELETE -> decoder.decodeSerializableValue(FrameMethodQueueDeleteSerializer)
            Frame.Method.Queue.Kind.DELETE_OK -> decoder.decodeSerializableValue(FrameMethodQueueDeleteOkSerializer)
            Frame.Method.Queue.Kind.UNBIND -> decoder.decodeSerializableValue(FrameMethodQueueUnbindSerializer)
            Frame.Method.Queue.Kind.UNBIND_OK -> Frame.Method.Queue.UnbindOk
        }
    }

}
