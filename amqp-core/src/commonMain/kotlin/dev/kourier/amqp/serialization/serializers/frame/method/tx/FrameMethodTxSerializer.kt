package dev.kourier.amqp.serialization.serializers.frame.method.tx

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodTxSerializer : KSerializer<Frame.Method.MethodTx> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodTx", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodTx) {
        TODO("Not yet implemented")
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodTx {
        TODO("Not yet implemented")
    }

}
