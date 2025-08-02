package dev.kourier.amqp.robust.declared

data class DeclaredQos(
    val count: UShort,
    val global: Boolean,
)
