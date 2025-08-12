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

    @Test
    fun testTableFromAndToMap() {
        val originalMap = mapOf(
            "boolean" to true,
            "int" to 42,
            "string" to "Hello",
            "nullValue" to null
        )

        val table = originalMap.toTable()
        assertEquals(
            mapOf(
                "boolean" to Field.Boolean(true),
                "int" to Field.Int(42),
                "string" to Field.LongString("Hello"),
                "nullValue" to Field.Null
            ),
            table
        )

        val convertedMap = table.toMap()
        assertEquals(originalMap, convertedMap)
    }

    @Test
    fun testTableFromTableOf() {
        val table = tableOf("boolean" to true)
        assertEquals(mapOf("boolean" to Field.Boolean(true)), table)
    }

}
