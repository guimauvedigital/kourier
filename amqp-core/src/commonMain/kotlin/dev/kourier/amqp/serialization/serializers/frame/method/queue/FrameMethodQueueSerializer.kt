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

object FrameMethodQueueSerializer : KSerializer<Frame.Method.MethodQueue> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodQueue", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodQueue) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.kind.value.toShort())
        when (value) {
            is Frame.Method.MethodQueue.Declare ->
                encoder.encodeSerializableValue(FrameMethodQueueDeclareSerializer, value.declare)

            is Frame.Method.MethodQueue.DeclareOk ->
                encoder.encodeSerializableValue(FrameMethodQueueDeclareOkSerializer, value.declareOk)

            is Frame.Method.MethodQueue.Bind ->
                encoder.encodeSerializableValue(FrameMethodQueueBindSerializer, value.bind)

            is Frame.Method.MethodQueue.BindOk -> {}
            is Frame.Method.MethodQueue.Purge ->
                encoder.encodeSerializableValue(FrameMethodQueuePurgeSerializer, value.purge)

            is Frame.Method.MethodQueue.PurgeOk ->
                encoder.encodeSerializableValue(FrameMethodQueuePurgeOkSerializer, value)

            is Frame.Method.MethodQueue.Delete ->
                encoder.encodeSerializableValue(FrameMethodQueueDeleteSerializer, value.delete)

            is Frame.Method.MethodQueue.DeleteOk ->
                encoder.encodeSerializableValue(FrameMethodQueueDeleteOkSerializer, value)

            is Frame.Method.MethodQueue.Unbind ->
                encoder.encodeSerializableValue(FrameMethodQueueUnbindSerializer, value.unbind)

            is Frame.Method.MethodQueue.UnbindOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodQueue {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.MethodQueue.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.MethodQueue.Kind.DECLARE ->
                Frame.Method.MethodQueue.Declare(decoder.decodeSerializableValue(FrameMethodQueueDeclareSerializer))

            Frame.Method.MethodQueue.Kind.DECLARE_OK ->
                Frame.Method.MethodQueue.DeclareOk(decoder.decodeSerializableValue(FrameMethodQueueDeclareOkSerializer))

            Frame.Method.MethodQueue.Kind.BIND ->
                Frame.Method.MethodQueue.Bind(decoder.decodeSerializableValue(FrameMethodQueueBindSerializer))

            Frame.Method.MethodQueue.Kind.BIND_OK -> Frame.Method.MethodQueue.BindOk
            Frame.Method.MethodQueue.Kind.PURGE ->
                Frame.Method.MethodQueue.Purge(decoder.decodeSerializableValue(FrameMethodQueuePurgeSerializer))

            Frame.Method.MethodQueue.Kind.PURGE_OK -> decoder.decodeSerializableValue(FrameMethodQueuePurgeOkSerializer)

            Frame.Method.MethodQueue.Kind.DELETE ->
                Frame.Method.MethodQueue.Delete(decoder.decodeSerializableValue(FrameMethodQueueDeleteSerializer))

            Frame.Method.MethodQueue.Kind.DELETE_OK ->
                decoder.decodeSerializableValue(FrameMethodQueueDeleteOkSerializer)

            Frame.Method.MethodQueue.Kind.UNBIND ->
                Frame.Method.MethodQueue.Unbind(decoder.decodeSerializableValue(FrameMethodQueueUnbindSerializer))

            Frame.Method.MethodQueue.Kind.UNBIND_OK -> Frame.Method.MethodQueue.UnbindOk
        }
    }

}
