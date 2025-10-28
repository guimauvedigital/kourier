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

object FrameMethodConnectionOpenOkSerializer : KSerializer<Frame.Method.Connection.OpenOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.OpenOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.OpenOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShortString(value.reserved1)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.OpenOk {
        require(decoder is ProtocolBinaryDecoder)

        return Frame.Method.Connection.OpenOk(decoder.decodeShortString().first)
    }

}
