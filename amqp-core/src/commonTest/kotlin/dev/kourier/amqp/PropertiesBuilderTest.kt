package dev.kourier.amqp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class PropertiesBuilderTest {

    @Test
    fun testPropertiesDsl() {
        val now = Instant.fromEpochMilliseconds(123456789L)
        val propertiesFromDsl = properties {
            contentType = "application/json"
            contentEncoding = "utf8"
            headers = mapOf("userId" to Field.LongString("123"))
            deliveryMode = 2u
            priority = 1u
            correlationId = "correlationID"
            replyTo = "replyTo"
            expiration = null
            messageId = "1"
            timestamp = now.toEpochMilliseconds()
            type = "type"
            userId = "userID"
            appId = "appID"
            reserved1 = "reserved1"
        }
        val properties = Properties(
            contentType = "application/json",
            contentEncoding = "utf8",
            headers = mapOf("userId" to Field.LongString("123")),
            deliveryMode = 2u,
            priority = 1u,
            correlationId = "correlationID",
            replyTo = "replyTo",
            expiration = null,
            messageId = "1",
            timestamp = now.toEpochMilliseconds(),
            type = "type",
            userId = "userID",
            appId = "appID",
            reserved1 = "reserved1"
        )
        assertEquals(properties, propertiesFromDsl)
    }

}
