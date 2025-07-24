package dev.kourier.amqp

/**
 * Creates a [Properties] using a DSL builder.
 */
fun properties(block: PropertiesBuilder.() -> Unit): Properties {
    return PropertiesBuilder().apply(block).build()
}
