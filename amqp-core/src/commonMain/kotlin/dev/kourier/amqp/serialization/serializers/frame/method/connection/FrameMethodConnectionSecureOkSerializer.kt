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

object FrameMethodConnectionSecureOkSerializer : KSerializer<Frame.Method.Connection.SecureOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.SecureOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.SecureOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeLongString(value.response)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.SecureOk {
        require(decoder is ProtocolBinaryDecoder)

        return Frame.Method.Connection.SecureOk(decoder.decodeLongString().first)
    }

}
