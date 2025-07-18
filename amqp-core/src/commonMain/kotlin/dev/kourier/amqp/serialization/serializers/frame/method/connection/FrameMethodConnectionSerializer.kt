package dev.kourier.amqp.serialization.serializers.frame.method.connection

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

object FrameMethodConnectionSerializer : KSerializer<Frame.Method.Connection> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.connectionKind.value.toShort())
        when (value) {
            is Frame.Method.Connection.Start ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartSerializer, value)

            is Frame.Method.Connection.StartOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartOkSerializer, value)

            is Frame.Method.Connection.Secure -> encoder.encodeLongString(value.challenge)
            is Frame.Method.Connection.SecureOk -> encoder.encodeLongString(value.response)
            is Frame.Method.Connection.Tune -> {
                encoder.encodeShort(value.channelMax.toShort())
                encoder.encodeInt(value.frameMax.toInt())
                encoder.encodeShort(value.heartbeat.toShort())
            }

            is Frame.Method.Connection.TuneOk -> {
                encoder.encodeShort(value.channelMax.toShort())
                encoder.encodeInt(value.frameMax.toInt())
                encoder.encodeShort(value.heartbeat.toShort())
            }

            is Frame.Method.Connection.Open ->
                encoder.encodeSerializableValue(FrameMethodConnectionOpenSerializer, value)

            is Frame.Method.Connection.OpenOk -> encoder.encodeShortString(value.reserved1)
            is Frame.Method.Connection.Close ->
                encoder.encodeSerializableValue(FrameMethodConnectionCloseSerializer, value)

            is Frame.Method.Connection.CloseOk -> {}
            is Frame.Method.Connection.Blocked -> encoder.encodeShortString(value.reason)
            is Frame.Method.Connection.Unblocked -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Connection.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Connection.Kind.START -> decoder.decodeSerializableValue(FrameMethodConnectionStartSerializer)
            Frame.Method.Connection.Kind.START_OK ->
                decoder.decodeSerializableValue(FrameMethodConnectionStartOkSerializer)

            Frame.Method.Connection.Kind.SECURE -> Frame.Method.Connection.Secure(decoder.decodeLongString().first)
            Frame.Method.Connection.Kind.SECURE_OK -> Frame.Method.Connection.SecureOk(decoder.decodeLongString().first)
            Frame.Method.Connection.Kind.TUNE -> {
                val channelMax = decoder.decodeShort().toUShort()
                val frameMax = decoder.decodeInt().toUInt()
                val heartbeat = decoder.decodeShort().toUShort()
                Frame.Method.Connection.Tune(channelMax, frameMax, heartbeat)
            }

            Frame.Method.Connection.Kind.TUNE_OK -> {
                val channelMax = decoder.decodeShort().toUShort()
                val frameMax = decoder.decodeInt().toUInt()
                val heartbeat = decoder.decodeShort().toUShort()
                Frame.Method.Connection.TuneOk(channelMax, frameMax, heartbeat)
            }

            Frame.Method.Connection.Kind.OPEN -> decoder.decodeSerializableValue(FrameMethodConnectionOpenSerializer)
            Frame.Method.Connection.Kind.OPEN_OK -> Frame.Method.Connection.OpenOk(decoder.decodeShortString().first)
            Frame.Method.Connection.Kind.CLOSE -> decoder.decodeSerializableValue(FrameMethodConnectionCloseSerializer)
            Frame.Method.Connection.Kind.CLOSE_OK -> Frame.Method.Connection.CloseOk
            Frame.Method.Connection.Kind.BLOCKED -> Frame.Method.Connection.Blocked(decoder.decodeShortString().first)
            Frame.Method.Connection.Kind.UNBLOCKED -> Frame.Method.Connection.Unblocked
        }
    }

}
