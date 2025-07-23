package dev.kourier.amqp

import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.runBlocking

fun withConnection(block: suspend (AMQPConnection) -> Unit) = runBlocking {
    val connection = createAMQPConnection(this, amqpConfig {})
    try {
        block(connection)
    } finally {
        connection.close()
    }
}
