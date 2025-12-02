package dev.kourier.amqp.states

import dev.kourier.amqp.Table

class DeclaredQueueBuilder {

    var name: String = ""
    var durable: Boolean = false
    var exclusive: Boolean = false
    var autoDelete: Boolean = false
    var arguments: Table = emptyMap()

    fun build(): DeclaredQueue {
        return DeclaredQueue(
            name = name,
            durable = durable,
            exclusive = exclusive,
            autoDelete = autoDelete,
            arguments = arguments,
        )
    }

}
