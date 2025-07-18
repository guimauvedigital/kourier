package dev.kourier.amqp

import dev.kourier.amqp.serialization.ProtocolBinary
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class TableTest {

    @Test
    fun testEncodeDecodeEmptyTable() {
        val table = Table(emptyMap())

        val encoded = ProtocolBinary.encodeToByteArray(table)
        val decoded = ProtocolBinary.decodeFromByteArray<Table>(encoded)

        assertEquals(table, decoded)
    }

    @Test
    fun testEncodeDecodeSimpleTable() {
        val table = Table(
            mapOf(
                "boolean" to Field.Boolean(true),
            )
        )

        val encoded = ProtocolBinary.encodeToByteArray(table)
        val decoded = ProtocolBinary.decodeFromByteArray<Table>(encoded)

        assertEquals(table, decoded)
    }

}
