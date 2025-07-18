package dev.kourier.amqp

import kotlin.test.Test

class AMQPChannelTest {

    @Test
    fun testExchange() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.exchangeDeclare("test1", "topic")

        channel.exchangeDelete("test1")

        // channel.close()
    }

}
