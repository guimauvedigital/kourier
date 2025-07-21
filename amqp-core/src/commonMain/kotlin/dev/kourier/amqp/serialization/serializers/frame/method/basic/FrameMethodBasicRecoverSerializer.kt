package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicRecoverSerializer : KSerializer<Frame.Method.Basic.Recover> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.Recover", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.Recover) {
        encoder.encodeBoolean(value.requeue)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.Recover {
        val requeue = decoder.decodeBoolean()

        return Frame.Method.Basic.Recover(
            requeue = requeue,
        )
    }

}
