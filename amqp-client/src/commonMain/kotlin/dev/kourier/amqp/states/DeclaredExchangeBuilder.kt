package dev.kourier.amqp.states

import dev.kourier.amqp.Table

class DeclaredExchangeBuilder {

    var name: String = ""
    var type: String = ""
    var durable: Boolean = false
    var autoDelete: Boolean = false
    var internal: Boolean = false
    var arguments: Table = emptyMap()

    fun build(): DeclaredExchange {
        return DeclaredExchange(
            name = name,
            type = type,
            durable = durable,
            autoDelete = autoDelete,
            internal = internal,
            arguments = arguments,
        )
    }

}
