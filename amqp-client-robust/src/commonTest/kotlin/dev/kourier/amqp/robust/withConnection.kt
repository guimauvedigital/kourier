package dev.kourier.amqp.robust

import dev.kourier.amqp.connection.AMQPConnection
import kotlinx.coroutines.runBlocking

fun withConnection(block: suspend (AMQPConnection) -> Unit) = runBlocking {
    val connection = createRobustAMQPConnection(this) {}
    try {
        block(connection)
    } finally {
        connection.close()
    }
}
