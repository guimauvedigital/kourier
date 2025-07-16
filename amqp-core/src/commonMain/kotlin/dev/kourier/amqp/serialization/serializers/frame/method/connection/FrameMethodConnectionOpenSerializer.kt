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

object FrameMethodConnectionOpenSerializer : KSerializer<Frame.Method.MethodConnection.ConnectionOpen> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodConnection.ConnectionOpen", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodConnection.ConnectionOpen) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.vhost)
        encoder.encodeShortString(value.reserved1)
        encoder.encodeBoolean(value.reserved2)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodConnection.ConnectionOpen {
        require(decoder is ProtocolBinaryDecoder)

        val vhost = decoder.decodeShortString().first
        val reserved1 = decoder.decodeShortString().first
        val reserved2 = decoder.decodeBoolean()

        return Frame.Method.MethodConnection.ConnectionOpen(
            vhost = vhost,
            reserved1 = reserved1,
            reserved2 = reserved2
        )
    }

}
