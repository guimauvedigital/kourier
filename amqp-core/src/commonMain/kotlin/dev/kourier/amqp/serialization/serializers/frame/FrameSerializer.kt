package dev.kourier.amqp.serialization.serializers.frame

import dev.kourier.amqp.Frame
import dev.kourier.amqp.ProtocolError
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import dev.kourier.amqp.serialization.serializers.frame.method.FrameMethodSerializer
import kotlinx.io.Buffer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameSerializer : KSerializer<Frame> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeByte(value.kind.value.toByte())
        encoder.encodeShort(value.channelId.toShort())

        when (val payload = value.payload) {
            is Frame.Method -> {
                val innerEncoder = ProtocolBinaryEncoder(Buffer())
                innerEncoder.encodeSerializableValue(FrameMethodSerializer, payload)
                encoder.encodeInt(innerEncoder.buffer.size.toInt())
                innerEncoder.buffer.copyTo(encoder.buffer)
            }

            is Frame.Header -> {
                val innerEncoder = ProtocolBinaryEncoder(Buffer())
                innerEncoder.encodeSerializableValue(FrameHeaderSerializer, payload)
                encoder.encodeInt(innerEncoder.buffer.size.toInt())
                innerEncoder.buffer.copyTo(encoder.buffer)
            }

            is Frame.Body -> {
                val size = payload.body.size
                encoder.encodeInt(size)
                encoder.encodeSerializableValue(ByteArraySerializer(), payload.body)
            }

            is Frame.Heartbeat -> {
                val size = 0
                encoder.encodeInt(size)
            }
        }

        encoder.encodeByte(206.toByte()) // endMarker
    }

    override fun deserialize(decoder: Decoder): Frame {
        val kind = decoder.decodeByte().toUByte().let { byte ->
            Frame.Kind.entries.first { it.value == byte }
        }
        val channelId = decoder.decodeShort().toUShort()
        val size = decoder.decodeInt().toULong()

        val result = when (kind) {
            Frame.Kind.METHOD -> {
                val payload = decoder.decodeSerializableValue(FrameMethodSerializer)
                Frame(channelId, payload)
            }

            Frame.Kind.HEADER -> {
                val payload = decoder.decodeSerializableValue(FrameHeaderSerializer)
                Frame(channelId, payload)
            }

            Frame.Kind.BODY -> {
                val body = decoder.decodeSerializableValue(ByteArraySerializer())
                Frame(channelId, Frame.Body(body))
            }

            Frame.Kind.HEARTBEAT -> Frame(channelId, Frame.Heartbeat)
        }

        val endMarker = decoder.decodeByte().toUByte()
        if (endMarker.toInt() != 206) throw ProtocolError.Invalid(endMarker, "Invalid end marker: $endMarker", this)

        return result
    }

}
