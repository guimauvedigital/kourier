package dev.kourier.amqp.serialization.serializers.frame.method.confirm

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodConfirmSerializer : KSerializer<Frame.Method.Confirm> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Confirm", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Confirm) {
        encoder.encodeShort(value.confirmKind.value.toShort())
        when (value) {
            is Frame.Method.Confirm.Select -> encoder.encodeSerializableValue(FrameMethodConfirmSelectSerializer, value)
            is Frame.Method.Confirm.SelectOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Confirm {
        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Confirm.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Confirm.Kind.SELECT -> decoder.decodeSerializableValue(FrameMethodConfirmSelectSerializer)
            Frame.Method.Confirm.Kind.SELECT_OK -> Frame.Method.Confirm.SelectOk
        }
    }

}
