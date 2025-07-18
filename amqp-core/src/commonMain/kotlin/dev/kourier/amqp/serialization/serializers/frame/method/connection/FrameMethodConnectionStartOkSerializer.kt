package dev.kourier.amqp.serialization.serializers.frame.method.connection

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import dev.kourier.amqp.serialization.serializers.TableSerializer
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodConnectionStartOkSerializer : KSerializer<Frame.Method.MethodConnection.StartOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodConnection.StartOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodConnection.StartOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeSerializableValue(TableSerializer, value.clientProperties)
        encoder.encodeShortString(value.mechanism)
        encoder.encodeLongString(value.response)
        encoder.encodeShortString(value.locale)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodConnection.StartOk {
        require(decoder is ProtocolBinaryDecoder)

        val clientProperties = decoder.decodeSerializableValue(TableSerializer)
        val mechanism = decoder.decodeShortString().first
        val response = decoder.decodeLongString().first
        val locale = decoder.decodeShortString().first

        return Frame.Method.MethodConnection.StartOk(
            clientProperties = clientProperties,
            mechanism = mechanism,
            response = response,
            locale = locale
        )
    }

}
