package dev.kourier.amqp.serialization.serializers.frame

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.serializers.PropertiesSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameHeaderSerializer : KSerializer<Frame.Header> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Header", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Header) {
        encoder.encodeShort(value.classID.toShort())
        encoder.encodeShort(value.weight.toShort())
        encoder.encodeLong(value.bodySize.toLong())
        encoder.encodeSerializableValue(PropertiesSerializer, value.properties)
    }

    override fun deserialize(decoder: Decoder): Frame.Header {
        val classID = decoder.decodeShort().toUShort()
        val weight = decoder.decodeShort().toUShort()
        val bodySize = decoder.decodeLong().toULong()
        val properties = decoder.decodeSerializableValue(PropertiesSerializer)

        return Frame.Header(
            classID = classID,
            weight = weight,
            bodySize = bodySize,
            properties = properties
        )
    }

}
