package dev.kourier.amqp.serialization.serializers.frame.method.basic

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodBasicSerializer : KSerializer<Frame.Method.MethodBasic> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodBasic", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodBasic) {
        TODO("Not yet implemented")
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodBasic {
        TODO("Not yet implemented")
    }

}
