package dev.kourier.amqp.states

data class DeclaredQos(
    val count: UShort,
    val global: Boolean,
)
