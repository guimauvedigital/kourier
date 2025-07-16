package dev.kourier.amqp.serialization.serializers

import dev.kourier.amqp.Field
import dev.kourier.amqp.ProtocolError
import dev.kourier.amqp.Table
import dev.kourier.amqp.serialization.ProtocolBinaryDecoder
import dev.kourier.amqp.serialization.ProtocolBinaryEncoder
import kotlinx.datetime.Instant
import kotlinx.io.Buffer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object TableSerializer : KSerializer<Table> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Table", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Table) {
        require(encoder is ProtocolBinaryEncoder)

        val innerEncoder = ProtocolBinaryEncoder(Buffer())
        for ((key, field) in value.values) {
            try {
                innerEncoder.encodeShortString(key)
            } catch (e: Exception) {
                throw ProtocolError.Encode(key, null, "Cannot write table key: $key", e)
            }
            try {
                writeField(field, innerEncoder)
            } catch (e: Exception) {
                throw ProtocolError.Encode(field, null, "Cannot write table value of key: $key", e)
            }
        }
        encoder.encodeInt(innerEncoder.buffer.size.toInt())
        innerEncoder.buffer.copyTo(encoder.buffer)
    }

    fun writeArray(values: List<Field>, encoder: ProtocolBinaryEncoder) {
        val innerEncoder = ProtocolBinaryEncoder(Buffer())
        for (value in values) {
            try {
                writeField(value, innerEncoder)
            } catch (e: Exception) {
                throw ProtocolError.Encode(value, null, "Cannot write array value: $value", e)
            }
        }
        encoder.encodeInt(innerEncoder.buffer.size.toInt())
        innerEncoder.buffer.copyTo(encoder.buffer)
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun writeField(field: Field, encoder: ProtocolBinaryEncoder) {
        encoder.encodeByte(field.kind.value.toByte())
        when (field) {
            is Field.Boolean -> encoder.encodeByte(if (field.value) 1 else 0)
            is Field.Int8 -> encoder.encodeByte(field.value)
            is Field.UInt8 -> encoder.encodeByte(field.value.toByte())
            is Field.Int16 -> encoder.encodeShort(field.value)
            is Field.UInt16 -> encoder.encodeShort(field.value.toShort())
            is Field.Int32 -> encoder.encodeInt(field.value)
            is Field.UInt32 -> encoder.encodeInt(field.value.toInt())
            is Field.Int64 -> encoder.encodeLong(field.value)
            is Field.Float -> encoder.encodeFloat(field.value)
            is Field.Double -> encoder.encodeDouble(field.value)
            is Field.LongString -> encoder.encodeLongString(field.value)
            is Field.Bytes -> encoder.encodeSerializableValue(ByteArraySerializer(), field.value)
            is Field.Array -> writeArray(field.value, encoder)
            is Field.Timestamp -> encoder.encodeLong(field.value.toEpochMilliseconds())
            is Field.Table -> encoder.encodeSerializableValue(TableSerializer, field.value)

            is Field.Decimal -> {
                encoder.encodeByte(field.scale.toByte())
                encoder.encodeInt(field.value.toInt())
            }

            is Field.Null -> encoder.encodeNull()
        }
    }

    override fun deserialize(decoder: Decoder): Table {
        require(decoder is ProtocolBinaryDecoder)

        val (table, _) = readTable(decoder)
        return table
    }

    private fun readTable(decoder: ProtocolBinaryDecoder): Pair<Table, Int> {
        val size = decoder.decodeInt()
        val result = mutableMapOf<String, Field>()
        var bytesRead = 0

        while (bytesRead < size) {
            val (key, keySize) = decoder.decodeShortString()
            bytesRead += keySize

            val (value, valueSize) = readField(decoder)
            bytesRead += valueSize

            result[key] = value
        }
        return Pair(Table(result), 4 + bytesRead)
    }

    private fun readArray(decoder: ProtocolBinaryDecoder): Pair<List<Field>, Int> {
        val size = decoder.decodeInt()
        val result = mutableListOf<Field>()
        var bytesRead = 0

        while (bytesRead < size) {
            val (value, valueSize) = readField(decoder)
            bytesRead += valueSize
            result.add(value)
        }

        return Pair(result, 4 + bytesRead)
    }

    private fun readField(decoder: ProtocolBinaryDecoder): Pair<Field, Int> {
        val kind = decoder.decodeByte().toUByte().let { byte ->
            Field.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Field.Kind.BOOLEAN -> {
                val value = decoder.decodeBoolean()
                Pair(Field.Boolean(value), 1 + 1)
            }

            Field.Kind.INT8 -> {
                val value = decoder.decodeByte()
                Pair(Field.Int8(value), 1 + 1)
            }

            Field.Kind.UINT8 -> {
                val value = decoder.decodeByte().toUByte()
                Pair(Field.UInt8(value), 1 + 1)
            }

            Field.Kind.INT16 -> {
                val value = decoder.decodeShort()
                Pair(Field.Int16(value), 1 + 2)
            }

            Field.Kind.UINT16 -> {
                val value = decoder.decodeShort().toUShort()
                Pair(Field.UInt16(value), 1 + 2)
            }

            Field.Kind.INT32 -> {
                val value = decoder.decodeInt()
                Pair(Field.Int32(value), 1 + 4)
            }

            Field.Kind.UINT32 -> {
                val value = decoder.decodeInt().toUInt()
                Pair(Field.UInt32(value), 1 + 4)
            }

            Field.Kind.INT64 -> {
                val value = decoder.decodeLong()
                Pair(Field.Int64(value), 1 + 8)
            }

            Field.Kind.FLOAT -> {
                val value = decoder.decodeFloat()
                Pair(Field.Float(value), 1 + 4)
            }

            Field.Kind.DOUBLE -> {
                val value = decoder.decodeDouble()
                Pair(Field.Double(value), 1 + 8)
            }

            Field.Kind.LONG_STRING -> {
                val (value, valueSize) = decoder.decodeLongString()
                Pair(Field.LongString(value), 1 + valueSize)
            }

            Field.Kind.BYTES -> {
                val size = decoder.decodeInt()
                val value = ByteArray(size)
                decoder.decodeSerializableValue(ByteArraySerializer())
                Pair(Field.Bytes(value), 1 + size)
            }

            Field.Kind.ARRAY -> {
                val (value, valueSize) = readArray(decoder)
                Pair(Field.Array(value), 1 + valueSize)
            }

            Field.Kind.TIMESTAMP -> {
                val timestamp = Instant.fromEpochMilliseconds(decoder.decodeLong())
                Pair(Field.Timestamp(timestamp), 1 + 8)
            }

            Field.Kind.TABLE -> {
                val (value, valueSize) = readTable(decoder)
                Pair(Field.Table(value), 1 + valueSize)
            }

            Field.Kind.DECIMAL -> {
                val scale = decoder.decodeByte().toUByte()
                val value = decoder.decodeInt().toUInt()
                Pair(Field.Decimal(scale, value), 1 + 1 + 4)
            }

            Field.Kind.NULL -> Pair(Field.Null, 1)
        }
    }

}
