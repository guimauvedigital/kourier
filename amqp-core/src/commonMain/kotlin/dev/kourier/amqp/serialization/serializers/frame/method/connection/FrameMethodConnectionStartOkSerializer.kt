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

object FrameMethodConnectionStartOkSerializer : KSerializer<Frame.Method.MethodConnection.ConnectionStartOk> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.MethodConnection.ConnectionStartOk", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.MethodConnection.ConnectionStartOk) {
        require(encoder is ProtocolBinaryEncoder)

        encoder.encodeSerializableValue(TableSerializer, value.clientProperties)
        encoder.encodeLongString(value.mechanism)
        encoder.encodeLongString(value.response)
        encoder.encodeLongString(value.locale)
    }

    override fun deserialize(decoder: Decoder): Frame.Method.MethodConnection.ConnectionStartOk {
        require(decoder is ProtocolBinaryDecoder)

        val clientProperties = decoder.decodeSerializableValue(TableSerializer)
        val mechanism = decoder.decodeLongString().first
        val response = decoder.decodeLongString().first
        val locale = decoder.decodeLongString().first

        return Frame.Method.MethodConnection.ConnectionStartOk(
            clientProperties = clientProperties,
            mechanism = mechanism,
            response = response,
            locale = locale
        )
    }

}
