package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicNackSerializer : KSerializer<Frame.Method.Basic.Nack> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Nack", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Nack) {
        encoder.encodeLong(value.deliveryTag.toLong())

        var bits = 0u
        if (value.multiple) bits = bits or (1u shl 0)
        if (value.requeue) bits = bits or (1u shl 1)

        encoder.encodeByte(bits.toByte())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Nack {
        val deliveryTag = decoder.decodeLong().toULong()

        val bits = decoder.decodeByte().toUInt()

        val multiple = bits and (1u shl 0) != 0u
        val requeue = bits and (1u shl 1) != 0u

        return Frame.Method.Basic.Nack(
            deliveryTag = deliveryTag,
            multiple = multiple,
            requeue = requeue,
        )
    }

}
