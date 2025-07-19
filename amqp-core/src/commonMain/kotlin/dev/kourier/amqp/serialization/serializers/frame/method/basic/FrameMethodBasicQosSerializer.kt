package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicQosSerializer : KSerializer<Frame.Method.Basic.Qos> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Qos", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Qos) {
        encoder.encodeInt(value.prefetchSize.toInt())
        encoder.encodeShort(value.prefetchCount.toShort())
        encoder.encodeBoolean(value.global)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Qos {
        val prefetchSize = decoder.decodeInt().toUInt()
        val prefetchCount = decoder.decodeShort().toUShort()
        val global = decoder.decodeBoolean()

        return Frame.Method.Basic.Qos(
            prefetchSize = prefetchSize,
            prefetchCount = prefetchCount,
            global = global
        )
    }

}
