package dev.kourier.amqp.serialization.serializers.frame.method.confirm

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodConfirmSelectSerializer : KSerializer<Frame.Method.Confirm.Select> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Confirm.Select", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Confirm.Select) {
        encoder.encodeBoolean(value.noWait)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Confirm.Select {
        val noWait = decoder.decodeBoolean()

        return Frame.Method.Confirm.Select(
            noWait = noWait,
        )
    }

}
