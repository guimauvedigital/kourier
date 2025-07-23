package dev.kourier.amqp

import kotlinx.datetime.Instant

sealed class Field {

    data class Boolean(val value: kotlin.Boolean) : Field()
    data class Byte(val value: kotlin.Byte) : Field()
    data class UByte(val value: kotlin.UByte) : Field()
    data class Short(val value: kotlin.Short) : Field()
    data class UShort(val value: kotlin.UShort) : Field()
    data class Int(val value: kotlin.Int) : Field()
    data class UInt(val value: kotlin.UInt) : Field()
    data class Long(val value: kotlin.Long) : Field()
    data class Float(val value: kotlin.Float) : Field()
    data class Double(val value: kotlin.Double) : Field()
    data class LongString(val value: String) : Field()
    data class Bytes(val value: ByteArray) : Field()
    data class Array(val value: List<Field>) : Field()
    data class Timestamp(val value: Instant) : Field()
    data class Table(val value: dev.kourier.amqp.Table) : Field()
    data class Decimal(val scale: kotlin.UByte, val value: kotlin.UInt) : Field()
    object Null : Field()

    val kind: Kind
        get() = when (this) {
            is Boolean -> Kind.BOOLEAN
            is Byte -> Kind.BYTE
            is UByte -> Kind.UBYTE
            is Short -> Kind.SHORT
            is UShort -> Kind.USHORT
            is Int -> Kind.INT
            is UInt -> Kind.UINT
            is Long -> Kind.LONG
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

    enum class Kind(val value: kotlin.UByte) {
        BOOLEAN(116u),      // t
        BYTE(98u),          // b
        UBYTE(66u),         // B
        SHORT(115u),        // s
        USHORT(117u),       // u
        INT(73u),           // I
        UINT(105u),         // i
        LONG(108u),         // l
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
