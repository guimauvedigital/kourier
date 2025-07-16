package dev.kourier.amqp

import dev.kourier.amqp.serialization.serializers.PropertiesSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PropertiesSerializer::class)
data class Properties(
    val contentType: String? = null,
    val contentEncoding: String? = null,
    val headers: Table? = null,
    val deliveryMode: UByte? = null,
    val priority: UByte? = null,
    val correlationID: String? = null,
    val replyTo: String? = null,
    val expiration: String? = null,
    val messageID: String? = null,
    val timestamp: Long? = null,
    val type: String? = null,
    val userID: String? = null,
    val appID: String? = null,
    val reserved1: String? = null,
) {

    object Flag {
        const val CONTENT_TYPE: UShort = 0x8000u
        const val CONTENT_ENCODING: UShort = 0x4000u
        const val HEADERS: UShort = 0x2000u
        const val DELIVERY_MODE: UShort = 0x1000u
        const val PRIORITY: UShort = 0x0800u
        const val CORRELATION_ID: UShort = 0x0400u
        const val REPLY_TO: UShort = 0x0200u
        const val EXPIRATION: UShort = 0x0100u
        const val MESSAGE_ID: UShort = 0x0080u
        const val TIMESTAMP: UShort = 0x0040u
        const val TYPE: UShort = 0x0020u
        const val USER_ID: UShort = 0x0010u
        const val APP_ID: UShort = 0x0008u
        const val RESERVED1: UShort = 0x0004u
    }

}
