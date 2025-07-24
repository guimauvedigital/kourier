package dev.kourier.amqp

class PropertiesBuilder {

    var contentType: String? = null
    var contentEncoding: String? = null
    var headers: Table? = null
    var deliveryMode: UByte? = null
    var priority: UByte? = null
    var correlationId: String? = null
    var replyTo: String? = null
    var expiration: String? = null
    var messageId: String? = null
    var timestamp: Long? = null
    var type: String? = null
    var userId: String? = null
    var appId: String? = null
    var reserved1: String? = null

    fun build(): Properties {
        return Properties(
            contentType = contentType,
            contentEncoding = contentEncoding,
            headers = headers,
            deliveryMode = deliveryMode,
            priority = priority,
            correlationId = correlationId,
            replyTo = replyTo,
            expiration = expiration,
            messageId = messageId,
            timestamp = timestamp,
            type = type,
            userId = userId,
            appId = appId,
            reserved1 = reserved1
        )
    }

}
