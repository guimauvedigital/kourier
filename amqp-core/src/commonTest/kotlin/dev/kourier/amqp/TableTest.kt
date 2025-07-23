package dev.kourier.amqp

import dev.kourier.amqp.serialization.ProtocolBinary
import dev.kourier.amqp.serialization.serializers.TableSerializer
import kotlin.test.Test
import kotlin.test.assertEquals

class TableTest {

    @Test
    fun testEncodeDecodeEmptyTable() {
        val table: Table = emptyMap()

        val encoded = ProtocolBinary.encodeToByteArray(TableSerializer, table)
        val decoded = ProtocolBinary.decodeFromByteArray(TableSerializer, encoded)

        assertEquals(table, decoded)
    }

    @Test
    fun testEncodeDecodeSimpleTable() {
        val table: Table = mapOf(
            "boolean" to Field.Boolean(true),
        )

        val encoded = ProtocolBinary.encodeToByteArray(TableSerializer, table)
        val decoded = ProtocolBinary.decodeFromByteArray(TableSerializer, encoded)

        assertEquals(table, decoded)
    }

}
