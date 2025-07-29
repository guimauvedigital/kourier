package dev.kourier.amqp.connection

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.withConnection
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class AMQPConnectionTest {

    @Test
    fun testConnectionWithUrl(): Unit = runBlocking {
        val urlString = "amqp://guest:guest@localhost:5672/"
        createAMQPConnection(this, urlString).close()
        createAMQPConnection(this, Url(urlString)).close()
        createAMQPConnection(this, amqpConfig(urlString)).close()
        createAMQPConnection(this, amqpConfig(Url(urlString))).close()
    }

    @Test
    fun testCanOpenChannelAndShutdown() = withConnection { connection ->
        val channel1 = connection.openChannel()
        assertEquals(1u, channel1.id)

        val channel2 = connection.openChannel()
        assertEquals(2u, channel2.id)

        val channel3 = connection.openChannel()
        assertEquals(3u, channel3.id)

        val channel4 = connection.openChannel()
        assertEquals(4u, channel4.id)
    }

    @Test
    fun testCloseMultipleTimes() = withConnection { connection ->
        assertFalse(connection.connectionClosed.isCompleted)
        connection.close()
        assertTrue(connection.connectionClosed.isCompleted)
        connection.close()
        assertTrue(connection.connectionClosed.isCompleted)
        assertFailsWith<AMQPException.ConnectionClosed> { connection.openChannel() }
    }

    @Test
    fun testHeartbeat() = withConnection { connection ->
        connection.sendHeartbeat()
    }

}
