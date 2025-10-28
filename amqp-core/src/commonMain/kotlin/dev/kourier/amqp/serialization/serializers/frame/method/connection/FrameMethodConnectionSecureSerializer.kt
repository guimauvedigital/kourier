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

object FrameMethodConnectionSecureSerializer : KSerializer<Frame.Method.Connection.Secure> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.Secure", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.Secure) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeLongString(value.challenge)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.Secure {
        require(decoder is ProtocolBinaryDecoder)

        return Frame.Method.Connection.Secure(decoder.decodeLongString().first)
    }

}
