package dev.kourier.amqp

import dev.kourier.amqp.serialization.ProtocolBinary
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertiesTest {

    @Test
    fun testEncodeDecodeProperties() {
        val now = Instant.fromEpochMilliseconds(123456789L)
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

        val encoded = ProtocolBinary.encodeToByteArray(properties)
        val decoded = ProtocolBinary.decodeFromByteArray<Properties>(encoded)

        assertEquals(
            listOf(
                254, 252, 16, 97, 112, 112, 108, 105, 99, 97, 116, 105, 111, 110, 47, 106, 115, 111, 110, 4, 117, 116,
                102, 56, 0, 0, 0, 15, 6, 117, 115, 101, 114, 73, 100, 83, 0, 0, 0, 3, 49, 50, 51, 2, 1, 13, 99, 111,
                114, 114, 101, 108, 97, 116, 105, 111, 110, 73, 68, 7, 114, 101, 112, 108, 121, 84, 111, 1, 49, 0, 0,
                0, 0, 7, 91, 205, 21, 4, 116, 121, 112, 101, 6, 117, 115, 101, 114, 73, 68, 5, 97, 112, 112, 73, 68,
                9, 114, 101, 115, 101, 114, 118, 101, 100, 49
            ),
            encoded.toList().map { it.toUByte().toInt() }
        )
        assertEquals(properties, decoded)
    }

}
