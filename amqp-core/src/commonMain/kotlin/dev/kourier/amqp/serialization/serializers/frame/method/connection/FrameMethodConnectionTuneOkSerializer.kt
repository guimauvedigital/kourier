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

object FrameMethodConnectionTuneOkSerializer : KSerializer<Frame.Method.Connection.TuneOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Connection.TuneOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Connection.TuneOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.channelMax.toShort())
        encoder.encodeInt(value.frameMax.toInt())
        encoder.encodeShort(value.heartbeat.toShort())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Connection.TuneOk {
        require(decoder is ProtocolBinaryDecoder)

        val channelMax = decoder.decodeShort().toUShort()
        val frameMax = decoder.decodeInt().toUInt()
        val heartbeat = decoder.decodeShort().toUShort()
        return Frame.Method.Connection.TuneOk(channelMax, frameMax, heartbeat)
    }

}
