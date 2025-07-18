package dev.kourier.amqp

import kotlin.test.Test

class AMQPChannelTest {

    @Test
    fun testExchange() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.exchangeDeclare("test1", "topic")
        channel.exchangeDeclare("test2", "topic")

        channel.exchangeBind("test1", "test2", "test")
        channel.exchangeUnbind("test1", "test2", "test")

        channel.exchangeDelete("test1")
        channel.exchangeDelete("test2")

        channel.close()
    }

}
