package dev.kourier.amqp

import kotlinx.datetime.Instant

/**
 * Creates a [Properties] using a DSL builder.
 */
fun properties(block: PropertiesBuilder.() -> Unit): Properties {
    return PropertiesBuilder().apply(block).build()
}

/**
 * Converts an [Any?] to a [Field], wrapping it in the appropriate [Field] type.
 */
fun Any?.toField(): Field =
    when (this) {
        null -> Field.Null
        is Field -> this
        is Boolean -> Field.Boolean(this)
        is Byte -> Field.Byte(this)
        is UByte -> Field.UByte(this)
        is Short -> Field.Short(this)
        is UShort -> Field.UShort(this)
        is Int -> Field.Int(this)
        is UInt -> Field.UInt(this)
        is Long -> Field.Long(this)
        is Float -> Field.Float(this)
        is Double -> Field.Double(this)
        is String -> Field.LongString(this)
        is ByteArray -> Field.Bytes(this)
        is List<*> -> Field.Array(this.mapNotNull { it.toField() })
        is Instant -> Field.Timestamp(this)
        is Map<*, *> -> Field.Table(this.toTable())
        else -> error("Unsupported type: ${this::class.simpleName}")
    }

/**
 * Converts a [Field] to an [Any?], unwrapping it to its native type.
 */
fun Field.toAny(): Any? =
    when (this) {
        is Field.Boolean -> value
        is Field.Byte -> value
        is Field.UByte -> value
        is Field.Short -> value
        is Field.UShort -> value
        is Field.Int -> value
        is Field.UInt -> value
        is Field.Long -> value
        is Field.Float -> value
        is Field.Double -> value
        is Field.LongString -> value
        is Field.Bytes -> value
        is Field.Array -> value.map { it.toAny() }
        is Field.Timestamp -> value
        is Field.Table -> value.toMap()
        is Field.Decimal -> Pair(scale, value)
        is Field.Null -> null
    }

/**
 * Converts a [Map] to a [Table], wrapping each value in a [Field].
 */
fun Map<*, *>.toTable(): Table =
    map { (key, value) -> key.toString() to value.toField() }.associate { it }

/**
 * Creates a [Table] from a variable number of key-value pairs.
 */
fun tableOf(vararg pairs: Pair<String, Any?>): Table = mapOf(*pairs).toTable()

/**
 * Converts a [Table] to a [Map], unwrapping each [Field] to its native type.
 */
fun Table.toMap(): Map<String, Any?> =
    mapValues { (_, value) -> value.toAny() }
