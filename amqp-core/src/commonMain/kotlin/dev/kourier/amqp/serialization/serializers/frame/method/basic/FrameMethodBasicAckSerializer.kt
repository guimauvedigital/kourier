package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicAckSerializer : KSerializer<Frame.Method.Basic.Ack> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Ack", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Ack) {
        encoder.encodeLong(value.deliveryTag.toLong())
        encoder.encodeBoolean(value.multiple)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Ack {
        val deliveryTag = decoder.decodeLong().toULong()
        val multiple = decoder.decodeBoolean()

        return Frame.Method.Basic.Ack(
            deliveryTag = deliveryTag,
            multiple = multiple,
        )
    }

}
