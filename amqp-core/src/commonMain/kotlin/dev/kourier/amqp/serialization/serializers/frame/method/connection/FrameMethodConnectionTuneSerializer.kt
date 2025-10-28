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

object FrameMethodConnectionTuneSerializer : KSerializer<Frame.Method.Connection.Tune> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.Tune", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.Tune) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.channelMax.toShort())
        encoder.encodeInt(value.frameMax.toInt())
        encoder.encodeShort(value.heartbeat.toShort())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.Tune {
        require(decoder is ProtocolBinaryDecoder)

        val channelMax = decoder.decodeShort().toUShort()
        val frameMax = decoder.decodeInt().toUInt()
        val heartbeat = decoder.decodeShort().toUShort()
        return Frame.Method.Connection.Tune(channelMax, frameMax, heartbeat)
    }

}
