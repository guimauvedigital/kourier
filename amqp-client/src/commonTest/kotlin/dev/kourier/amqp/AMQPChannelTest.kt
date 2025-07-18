package dev.kourier.amqp

import kotlin.test.Test
import kotlin.test.assertEquals

class AMQPChannelTest {

    @Test
    fun testQueue() = withConnection { connection ->
        val channel = connection.openChannel()

        val queueDeclare = channel.queueDeclare("test", durable = false)
        assertEquals("test", queueDeclare.queueName)

        channel.queueBind("test", "amq.topic", "test")
        channel.queueUnbind("test", "amq.topic", "test")

        channel.queuePurge("test")
        channel.queueDelete("test")

        channel.close()
    }

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
