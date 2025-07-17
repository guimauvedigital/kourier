package dev.kourier.amqp

import kotlinx.coroutines.runBlocking

fun withConnection(block: suspend (AMQPConnection) -> Unit) = runBlocking {
    val connection = AMQPConnection.connect(
        this, AMQPConnectionConfiguration(
            AMQPConnectionConfiguration.Connection.Plain,
            AMQPConnectionConfiguration.Server()
        )
    )
    try {
        block(connection)
    } finally {
        connection.close()
    }
}
