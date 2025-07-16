package dev.kourier.amqp.serialization.serializers.frame.method.exchange

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

object FrameMethodExchangeDeleteSerializer : KSerializer<Frame.Method.MethodExchange.ExchangeDelete> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodExchange.ExchangeDelete", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodExchange.ExchangeDelete) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.exchangeName)

        var bits = 0u
        if (value.ifUnused) bits = bits or (1u shl 0)
        if (value.noWait) bits = bits or (1u shl 1)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodExchange.ExchangeDelete {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val exchangeName = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val ifUnused = bits and (1u shl 0) != 0u
        val noWait = bits and (1u shl 1) != 0u

        return Frame.Method.MethodExchange.ExchangeDelete(
            reserved1 = reserved1,
            exchangeName = exchangeName,
            ifUnused = ifUnused,
            noWait = noWait,
        )
    }

}
