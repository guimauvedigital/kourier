package dev.kourier.amqp.serialization.serializers.frame.method.connection

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import dev.kourier.amqp.serialization.serializers.TableSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodConnectionStartSerializer : KSerializer<Frame.Method.MethodConnection.ConnectionStart> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodConnection.ConnectionStart", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodConnection.ConnectionStart) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeByte(value.versionMajor.toByte())
        encoder.encodeByte(value.versionMinor.toByte())
        encoder.encodeSerializableValue(TableSerializer, value.serverProperties)
        encoder.encodeLongString(value.mechanisms)
        encoder.encodeLongString(value.locales)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodConnection.ConnectionStart {
        require(decoder is ProtocolBinaryDecoder)

        val versionMajor = decoder.decodeByte().toUByte()
        val versionMinor = decoder.decodeByte().toUByte()
        val serverProperties = decoder.decodeSerializableValue(TableSerializer)
        val mechanisms = decoder.decodeLongString().first
        val locales = decoder.decodeLongString().first

        return Frame.Method.MethodConnection.ConnectionStart(
            versionMajor = versionMajor,
            versionMinor = versionMinor,
            serverProperties = serverProperties,
            mechanisms = mechanisms,
            locales = locales
        )
    }

}
