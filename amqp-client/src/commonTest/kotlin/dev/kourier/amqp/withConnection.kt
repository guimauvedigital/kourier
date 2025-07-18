package dev.kourier.amqp

import dev.kourier.amqp.dsl.amqpConnectionConfiguration
import kotlinx.coroutines.runBlocking

fun withConnection(block: suspend (AMQPConnection) -> Unit) = runBlocking {
    val connection = AMQPConnection.connect(this, amqpConnectionConfiguration {})
    try {
        block(connection)
    } finally {
        connection.close()
    }
}
