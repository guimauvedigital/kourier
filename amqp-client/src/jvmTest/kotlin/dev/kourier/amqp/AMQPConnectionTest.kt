package dev.kourier.amqp

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class AMQPConnectionTest {

    @Test
    fun testCanOpenChannelAndShutdown() = runBlocking {
        val connection = AMQPConnection.connect(
            this, AMQPConnectionConfiguration(
                AMQPConnectionConfiguration.Connection.Plain,
                AMQPConnectionConfiguration.Server()
            )
        )

        // TODO: Test opening channels

        connection.close()
    }

    @Test
    fun testCloseMultipleTimes() = runBlocking {
        val connection = AMQPConnection.connect(
            this, AMQPConnectionConfiguration(
                AMQPConnectionConfiguration.Connection.Plain,
                AMQPConnectionConfiguration.Server()
            )
        )

        connection.close()
        connection.close()
    }

}
