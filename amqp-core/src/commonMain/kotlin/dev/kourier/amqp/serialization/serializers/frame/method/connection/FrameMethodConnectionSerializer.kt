package dev.kourier.amqp.serialization.serializers.frame.method.connection

import dev.kourier.amqp.Frame
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
        encoder.encodeShort(value.connectionKind.value.toShort())
        when (value) {
            is Frame.Method.Connection.Start ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartSerializer, value)

            is Frame.Method.Connection.StartOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionStartOkSerializer, value)

            is Frame.Method.Connection.Secure ->
                encoder.encodeSerializableValue(FrameMethodConnectionSecureSerializer, value)

            is Frame.Method.Connection.SecureOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionSecureOkSerializer, value)

            is Frame.Method.Connection.Tune ->
                encoder.encodeSerializableValue(FrameMethodConnectionTuneSerializer, value)

            is Frame.Method.Connection.TuneOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionTuneOkSerializer, value)

            is Frame.Method.Connection.Open ->
                encoder.encodeSerializableValue(FrameMethodConnectionOpenSerializer, value)

            is Frame.Method.Connection.OpenOk ->
                encoder.encodeSerializableValue(FrameMethodConnectionOpenOkSerializer, value)

            is Frame.Method.Connection.Close ->
                encoder.encodeSerializableValue(FrameMethodConnectionCloseSerializer, value)

            is Frame.Method.Connection.CloseOk -> {}
            is Frame.Method.Connection.Blocked ->
                encoder.encodeSerializableValue(FrameMethodConnectionBlockedSerializer, value)

            is Frame.Method.Connection.Unblocked -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection {
        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Connection.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Connection.Kind.START -> decoder.decodeSerializableValue(FrameMethodConnectionStartSerializer)
            Frame.Method.Connection.Kind.START_OK ->
                decoder.decodeSerializableValue(FrameMethodConnectionStartOkSerializer)

            Frame.Method.Connection.Kind.SECURE -> decoder.decodeSerializableValue(FrameMethodConnectionSecureSerializer)
            Frame.Method.Connection.Kind.SECURE_OK -> decoder.decodeSerializableValue(
                FrameMethodConnectionSecureOkSerializer
            )

            Frame.Method.Connection.Kind.TUNE -> decoder.decodeSerializableValue(FrameMethodConnectionTuneSerializer)
            Frame.Method.Connection.Kind.TUNE_OK -> decoder.decodeSerializableValue(
                FrameMethodConnectionTuneOkSerializer
            )

            Frame.Method.Connection.Kind.OPEN -> decoder.decodeSerializableValue(FrameMethodConnectionOpenSerializer)
            Frame.Method.Connection.Kind.OPEN_OK -> decoder.decodeSerializableValue(
                FrameMethodConnectionOpenOkSerializer
            )

            Frame.Method.Connection.Kind.CLOSE -> decoder.decodeSerializableValue(FrameMethodConnectionCloseSerializer)
            Frame.Method.Connection.Kind.CLOSE_OK -> Frame.Method.Connection.CloseOk
            Frame.Method.Connection.Kind.BLOCKED -> decoder.decodeSerializableValue(
                FrameMethodConnectionBlockedSerializer
            )

            Frame.Method.Connection.Kind.UNBLOCKED -> Frame.Method.Connection.Unblocked
        }
    }

}
