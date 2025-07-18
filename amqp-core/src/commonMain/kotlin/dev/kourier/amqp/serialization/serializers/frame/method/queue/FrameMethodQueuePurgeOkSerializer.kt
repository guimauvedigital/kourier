package dev.kourier.amqp.serialization.serializers.frame.method.queue

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodQueuePurgeOkSerializer : KSerializer<Frame.Method.Queue.PurgeOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Queue.PurgeOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Queue.PurgeOk) {
        encoder.encodeInt(value.messageCount.toInt())
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Queue.PurgeOk {
        val messageCount = decoder.decodeInt().toUInt()

        return Frame.Method.Queue.PurgeOk(
            messageCount = messageCount,
        )
    }

}
