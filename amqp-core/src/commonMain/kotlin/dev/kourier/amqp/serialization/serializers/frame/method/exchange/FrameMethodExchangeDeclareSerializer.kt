package dev.kourier.amqp.serialization.serializers.frame.method.exchange

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

object FrameMethodExchangeDeclareSerializer : KSerializer<Frame.Method.Exchange.Declare> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Exchange.Declare", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Exchange.Declare) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.exchangeName)
        encoder.encodeShortString(value.exchangeType)

        var bits = 0u
        if (value.passive) bits = bits or (1u shl 0)
        if (value.durable) bits = bits or (1u shl 1)
        if (value.autoDelete) bits = bits or (1u shl 2)
        if (value.internal) bits = bits or (1u shl 3)
        if (value.noWait) bits = bits or (1u shl 4)

        encoder.encodeByte(bits.toByte())
        encoder.encodeSerializableValue(TableSerializer, value.arguments)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Exchange.Declare {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val exchangeName = decoder.decodeShortString().first
        val exchangeType = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val passive = bits and (1u shl 0) != 0u
        val durable = bits and (1u shl 1) != 0u
        val autoDelete = bits and (1u shl 2) != 0u
        val internal = bits and (1u shl 3) != 0u
        val noWait = bits and (1u shl 4) != 0u

        val arguments = decoder.decodeSerializableValue(TableSerializer)

        return Frame.Method.Exchange.Declare(
            reserved1 = reserved1,
            exchangeName = exchangeName,
            exchangeType = exchangeType,
            passive = passive,
            durable = durable,
            autoDelete = autoDelete,
            internal = internal,
            noWait = noWait,
            arguments = arguments
        )
    }

}
