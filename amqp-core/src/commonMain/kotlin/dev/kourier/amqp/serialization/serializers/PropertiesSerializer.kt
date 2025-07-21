package dev.kourier.amqp.serialization.serializers

import dev.kourier.amqp.Properties
import dev.kourier.amqp.ProtocolError
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object PropertiesSerializer : KSerializer<Properties> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Properties", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Properties) {
        require(encoder is ProtocolBinaryEncoder)

        var flags: UShort = 0u
        if (value.contentType != null) flags = flags or Properties.Flag.CONTENT_TYPE
        if (value.contentEncoding != null) flags = flags or Properties.Flag.CONTENT_ENCODING
        if (value.headers != null) flags = flags or Properties.Flag.HEADERS
        if (value.deliveryMode != null) flags = flags or Properties.Flag.DELIVERY_MODE
        if (value.priority != null) flags = flags or Properties.Flag.PRIORITY
        if (value.correlationId != null) flags = flags or Properties.Flag.CORRELATION_ID
        if (value.replyTo != null) flags = flags or Properties.Flag.REPLY_TO
        if (value.expiration != null) flags = flags or Properties.Flag.EXPIRATION
        if (value.messageId != null) flags = flags or Properties.Flag.MESSAGE_ID
        if (value.timestamp != null) flags = flags or Properties.Flag.TIMESTAMP
        if (value.type != null) flags = flags or Properties.Flag.TYPE
        if (value.userId != null) flags = flags or Properties.Flag.USER_ID
        if (value.appId != null) flags = flags or Properties.Flag.APP_ID
        if (value.reserved1 != null) flags = flags or Properties.Flag.RESERVED1

        encoder.encodeShort(flags.toShort())

        value.contentType?.let { encoder.encodeShortString(it) }
        value.contentEncoding?.let { encoder.encodeShortString(it) }
        value.headers?.let { encoder.encodeSerializableValue(TableSerializer, it) }
        value.deliveryMode?.let { encoder.encodeByte(it.toByte()) }
        value.priority?.let { encoder.encodeByte(it.toByte()) }
        value.correlationId?.let { encoder.encodeShortString(it) }
        value.replyTo?.let { encoder.encodeShortString(it) }
        value.expiration?.let { encoder.encodeShortString(it) }
        value.messageId?.let { encoder.encodeShortString(it) }
        value.timestamp?.let { encoder.encodeLong(it) }
        value.type?.let { encoder.encodeShortString(it) }
        value.userId?.let { encoder.encodeShortString(it) }
        value.appId?.let { encoder.encodeShortString(it) }
        value.reserved1?.let { encoder.encodeShortString(it) }
    }

    override fun deserialize(decoder: Decoder): Properties {
        require(decoder is ProtocolBinaryDecoder)

        // Read flags
        val flags = decoder.decodeShort().toUShort()

        // Validate flags if needed (example logic, adjust as necessary)
        val invalid = (flags and 1u > 0u) || (flags and 2u > 0u)
        if (invalid) throw ProtocolError.Invalid(flags, "Invalid flags: $flags", this)

        val contentType = if (flags and Properties.Flag.CONTENT_TYPE > 0u) decoder.decodeShortString().first else null
        val contentEncoding =
            if (flags and Properties.Flag.CONTENT_ENCODING > 0u) decoder.decodeShortString().first else null
        val headers =
            if (flags and Properties.Flag.HEADERS > 0u) decoder.decodeSerializableValue(TableSerializer) else null
        val deliveryMode = if (flags and Properties.Flag.DELIVERY_MODE > 0u) decoder.decodeByte().toUByte() else null
        val priority = if (flags and Properties.Flag.PRIORITY > 0u) decoder.decodeByte().toUByte() else null
        val correlationID =
            if (flags and Properties.Flag.CORRELATION_ID > 0u) decoder.decodeShortString().first else null
        val replyTo = if (flags and Properties.Flag.REPLY_TO > 0u) decoder.decodeShortString().first else null
        val expiration = if (flags and Properties.Flag.EXPIRATION > 0u) decoder.decodeShortString().first else null
        val messageID = if (flags and Properties.Flag.MESSAGE_ID > 0u) decoder.decodeShortString().first else null
        val timestamp = if (flags and Properties.Flag.TIMESTAMP > 0u) decoder.decodeLong() else null
        val type = if (flags and Properties.Flag.TYPE > 0u) decoder.decodeShortString().first else null
        val userID = if (flags and Properties.Flag.USER_ID > 0u) decoder.decodeShortString().first else null
        val appID = if (flags and Properties.Flag.APP_ID > 0u) decoder.decodeShortString().first else null
        val reserved1 = if (flags and Properties.Flag.RESERVED1 > 0u) decoder.decodeShortString().first else null

        return Properties(
            contentType = contentType,
            contentEncoding = contentEncoding,
            headers = headers,
            deliveryMode = deliveryMode,
            priority = priority,
            correlationId = correlationID,
            replyTo = replyTo,
            expiration = expiration,
            messageId = messageID,
            timestamp = timestamp,
            type = type,
            userId = userID,
            appId = appID,
            reserved1 = reserved1
        )
    }

}
