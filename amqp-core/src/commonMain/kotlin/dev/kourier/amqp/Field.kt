package dev.kourier.amqp

import kotlinx.datetime.Instant

sealed class Field {

    data class Boolean(val value: kotlin.Boolean) : Field()
    data class Int8(val value: Byte) : Field()
    data class UInt8(val value: UByte) : Field()
    data class Int16(val value: Short) : Field()
    data class UInt16(val value: UShort) : Field()
    data class Int32(val value: Int) : Field()
    data class UInt32(val value: UInt) : Field()
    data class Int64(val value: Long) : Field()
    data class Float(val value: kotlin.Float) : Field()
    data class Double(val value: kotlin.Double) : Field()
    data class LongString(val value: String) : Field()
    data class Bytes(val value: ByteArray) : Field()
    data class Array(val value: List<Field>) : Field()
    data class Timestamp(val value: Instant) : Field()
    data class Table(val value: dev.kourier.amqp.Table) : Field()
    data class Decimal(val scale: UByte, val value: UInt) : Field()
    object Null : Field()

    val kind: Kind
        get() = when (this) {
            is Boolean -> Kind.BOOLEAN
            is Int8 -> Kind.INT8
            is UInt8 -> Kind.UINT8
            is Int16 -> Kind.INT16
            is UInt16 -> Kind.UINT16
            is Int32 -> Kind.INT32
            is UInt32 -> Kind.UINT32
            is Int64 -> Kind.INT64
            is Float -> Kind.FLOAT
            is Double -> Kind.DOUBLE
            is LongString -> Kind.LONG_STRING
            is Bytes -> Kind.BYTES
            is Array -> Kind.ARRAY
            is Timestamp -> Kind.TIMESTAMP
            is Table -> Kind.TABLE
            is Decimal -> Kind.DECIMAL
            is Null -> Kind.NULL
        }

    enum class Kind(val value: UByte) {
        BOOLEAN(116u),      // t
        INT8(98u),          // b
        UINT8(66u),         // B
        INT16(115u),        // s
        UINT16(117u),       // u
        INT32(73u),         // I
        UINT32(105u),       // i
        INT64(108u),        // l
        FLOAT(102u),        // f
        DOUBLE(100u),       // d
        LONG_STRING(83u),   // S
        BYTES(120u),        // x
        ARRAY(65u),         // A
        TIMESTAMP(84u),     // T
        TABLE(70u),         // F
        DECIMAL(68u),       // D
        NULL(86u)           // V
    }

}
