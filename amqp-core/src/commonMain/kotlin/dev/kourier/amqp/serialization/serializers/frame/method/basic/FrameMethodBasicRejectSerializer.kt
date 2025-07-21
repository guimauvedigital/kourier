package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicRejectSerializer : KSerializer<Frame.Method.Basic.Reject> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Reject", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Reject) {
        encoder.encodeLong(value.deliveryTag.toLong())
        encoder.encodeBoolean(value.requeue)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Reject {
        val deliveryTag = decoder.decodeLong().toULong()
        val requeue = decoder.decodeBoolean()

        return Frame.Method.Basic.Reject(
            deliveryTag = deliveryTag,
            requeue = requeue,
        )
    }

}
