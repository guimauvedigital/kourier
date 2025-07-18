package dev.kourier.amqp

import kotlin.test.Test
import kotlin.test.assertEquals

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
        connection.close()
        connection.close()
    }

}
