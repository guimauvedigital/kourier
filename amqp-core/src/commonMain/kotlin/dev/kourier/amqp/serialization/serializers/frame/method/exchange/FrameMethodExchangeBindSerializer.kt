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

object FrameMethodExchangeBindSerializer : KSerializer<Frame.Method.Exchange.Bind> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Exchange.Bind", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Exchange.Bind) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeShort(value.reserved1.toShort())
        encoder.encodeShortString(value.destination)
        encoder.encodeShortString(value.source)
        encoder.encodeShortString(value.routingKey)

        var bits = 0u
        if (value.noWait) bits = bits or (1u shl 0)

        encoder.encodeByte(bits.toByte())
        encoder.encodeSerializableValue(TableSerializer, value.arguments)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Exchange.Bind {
        require(decoder is ProtocolBinaryDecoder)

        val reserved1 = decoder.decodeShort().toUShort()
        val destination = decoder.decodeShortString().first
        val source = decoder.decodeShortString().first
        val routingKey = decoder.decodeShortString().first

        val bits = decoder.decodeByte().toUInt()

        val noWait = bits and (1u shl 0) != 0u

        val arguments = decoder.decodeSerializableValue(TableSerializer)

        return Frame.Method.Exchange.Bind(
            reserved1 = reserved1,
            destination = destination,
            source = source,
            routingKey = routingKey,
            noWait = noWait,
            arguments = arguments,
        )
    }

}
