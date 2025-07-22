package dev.kourier.amqp.connection

import dev.kourier.amqp.withConnection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AMQPConnectionTest {

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
    }

    @Test
    fun testHeartbeat() = withConnection { connection ->
        connection.sendHeartbeat()
    }

}
