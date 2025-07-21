package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicRecoverAsyncSerializer : KSerializer<Frame.Method.Basic.RecoverAsync> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Basic.RecoverAsync", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Basic.RecoverAsync) {
        encoder.encodeBoolean(value.requeue)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Basic.RecoverAsync {
        val requeue = decoder.decodeBoolean()

        return Frame.Method.Basic.RecoverAsync(
            requeue = requeue,
        )
    }

}
