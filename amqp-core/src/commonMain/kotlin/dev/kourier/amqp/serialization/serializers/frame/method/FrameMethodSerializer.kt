package dev.kourier.amqp.serialization.serializers.frame.method

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.serializers.frame.method.basic.FrameMethodBasicSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.channel.FrameMethodChannelSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.confirm.FrameMethodConfirmSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.connection.FrameMethodConnectionSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.exchange.FrameMethodExchangeSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.queue.FrameMethodQueueSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.tx.FrameMethodTxSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodSerializer : KSerializer<Frame.Method> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method) {
        encoder.encodeShort(value.kind.value.toShort())
        when (value) {
            is Frame.Method.Connection ->
                encoder.encodeSerializableValue(FrameMethodConnectionSerializer, value.connection)

            is Frame.Method.Channel ->
                encoder.encodeSerializableValue(FrameMethodChannelSerializer, value.channel)

            is Frame.Method.Exchange ->
                encoder.encodeSerializableValue(FrameMethodExchangeSerializer, value.exchange)

            is Frame.Method.Queue ->
                encoder.encodeSerializableValue(FrameMethodQueueSerializer, value.queue)

            is Frame.Method.Basic ->
                encoder.encodeSerializableValue(FrameMethodBasicSerializer, value.basic)

            is Frame.Method.Confirm ->
                encoder.encodeSerializableValue(FrameMethodConfirmSerializer, value.confirm)

            is Frame.Method.Tx ->
                encoder.encodeSerializableValue(FrameMethodTxSerializer, value.tx)
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method {
        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Kind.CONNECTION ->
                Frame.Method.Connection(decoder.decodeSerializableValue(FrameMethodConnectionSerializer))

            Frame.Method.Kind.CHANNEL ->
                Frame.Method.Channel(decoder.decodeSerializableValue(FrameMethodChannelSerializer))

            Frame.Method.Kind.EXCHANGE ->
                Frame.Method.Exchange(decoder.decodeSerializableValue(FrameMethodExchangeSerializer))

            Frame.Method.Kind.QUEUE ->
                Frame.Method.Queue(decoder.decodeSerializableValue(FrameMethodQueueSerializer))

            Frame.Method.Kind.BASIC ->
                Frame.Method.Basic(decoder.decodeSerializableValue(FrameMethodBasicSerializer))

            Frame.Method.Kind.CONFIRM ->
                Frame.Method.Confirm(decoder.decodeSerializableValue(FrameMethodConfirmSerializer))

            Frame.Method.Kind.TX ->
                Frame.Method.Tx(decoder.decodeSerializableValue(FrameMethodTxSerializer))
        }
    }

}
