package dev.kourier.amqp.states

class DeclaredQosBuilder {

    var count: UShort = 0u
    var global: Boolean = false

    fun build(): DeclaredQos {
        return DeclaredQos(
            count = count,
            global = global,
        )
    }

}
