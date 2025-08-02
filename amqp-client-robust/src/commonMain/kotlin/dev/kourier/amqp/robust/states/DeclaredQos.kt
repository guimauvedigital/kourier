package dev.kourier.amqp.robust.states

data class DeclaredQos(
    val count: UShort,
    val global: Boolean,
)
