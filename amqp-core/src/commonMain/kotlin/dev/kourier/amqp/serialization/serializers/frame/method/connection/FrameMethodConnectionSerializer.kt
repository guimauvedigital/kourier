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

object FrameMethodConnectionSerializer : KSerializer<Frame.Method.MethodConnection> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodConnection", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodConnection) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.kind.value.toShort())
        when (value) {
            is Frame.Method.MethodConnection.Start ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartSerializer, value.start)

            is Frame.Method.MethodConnection.StartOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartOkSerializer, value.startOk)

            is Frame.Method.MethodConnection.Secure -> encoder.encodeLongString(value.challenge)
            is Frame.Method.MethodConnection.SecureOk -> encoder.encodeLongString(value.response)
            is Frame.Method.MethodConnection.Tune -> {
                encoder.encodeShort(value.channelMax.toShort())
                encoder.encodeInt(value.frameMax.toInt())
                encoder.encodeShort(value.heartbeat.toShort())
            }

            is Frame.Method.MethodConnection.TuneOk -> {
                encoder.encodeShort(value.channelMax.toShort())
                encoder.encodeInt(value.frameMax.toInt())
                encoder.encodeShort(value.heartbeat.toShort())
            }

            is Frame.Method.MethodConnection.Open ->
                encoder.encodeSerializableValue(FrameMethodConnectionOpenSerializer, value.open)

            is Frame.Method.MethodConnection.OpenOk -> encoder.encodeShortString(value.reserved1)
            is Frame.Method.MethodConnection.Close ->
                encoder.encodeSerializableValue(FrameMethodConnectionCloseSerializer, value.close)

            is Frame.Method.MethodConnection.CloseOk -> {}
            is Frame.Method.MethodConnection.Blocked -> encoder.encodeShortString(value.reason)
            is Frame.Method.MethodConnection.Unblocked -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodConnection {
        require(decoder is ProtocolBinaryDecoder)

        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.MethodConnection.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.MethodConnection.Kind.START ->
                Frame.Method.MethodConnection.Start(decoder.decodeSerializableValue(FrameMethodConnectionStartSerializer))

            Frame.Method.MethodConnection.Kind.START_OK ->
                Frame.Method.MethodConnection.StartOk(
                    decoder.decodeSerializableValue(FrameMethodConnectionStartOkSerializer)
                )

            Frame.Method.MethodConnection.Kind.SECURE ->
                Frame.Method.MethodConnection.Secure(decoder.decodeLongString().first)

            Frame.Method.MethodConnection.Kind.SECURE_OK ->
                Frame.Method.MethodConnection.SecureOk(decoder.decodeLongString().first)

            Frame.Method.MethodConnection.Kind.TUNE -> {
                val channelMax = decoder.decodeShort().toUShort()
                val frameMax = decoder.decodeInt().toUInt()
                val heartbeat = decoder.decodeShort().toUShort()
                Frame.Method.MethodConnection.Tune(channelMax, frameMax, heartbeat)
            }

            Frame.Method.MethodConnection.Kind.TUNE_OK -> {
                val channelMax = decoder.decodeShort().toUShort()
                val frameMax = decoder.decodeInt().toUInt()
                val heartbeat = decoder.decodeShort().toUShort()
                Frame.Method.MethodConnection.TuneOk(channelMax, frameMax, heartbeat)
            }

            Frame.Method.MethodConnection.Kind.OPEN ->
                Frame.Method.MethodConnection.Open(decoder.decodeSerializableValue(FrameMethodConnectionOpenSerializer))

            Frame.Method.MethodConnection.Kind.OPEN_OK ->
                Frame.Method.MethodConnection.OpenOk(decoder.decodeShortString().first)

            Frame.Method.MethodConnection.Kind.CLOSE ->
                Frame.Method.MethodConnection.Close(decoder.decodeSerializableValue(FrameMethodConnectionCloseSerializer))

            Frame.Method.MethodConnection.Kind.CLOSE_OK -> Frame.Method.MethodConnection.CloseOk
            Frame.Method.MethodConnection.Kind.BLOCKED ->
                Frame.Method.MethodConnection.Blocked(decoder.decodeShortString().first)

            Frame.Method.MethodConnection.Kind.UNBLOCKED -> Frame.Method.MethodConnection.Unblocked
        }
    }

}
