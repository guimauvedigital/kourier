package dev.kourier.amqp

import io.ktor.utils.io.core.*
import kotlin.test.Test
import kotlin.test.assertEquals

class AMQPChannelTest {

    @Test
    fun testCanCloseChannel() = withConnection { connection ->
        val channel = connection.openChannel()
        channel.close()
    }

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

    @Test
    fun testBasicPublish() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.queueDeclare("test", durable = true)

        val body = "{}".toByteArray()

        val result = channel.basicPublish(body = body, exchange = "", routingKey = "test")
        assertEquals(0u, result.deliveryTag)

        channel.queueDelete("test")

        channel.close()
    }

    @Test
    fun testBasicGet() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.queueDeclare("test", durable = true)

        // TODO

        channel.queueDelete("test")

        channel.close()
    }

    @Test
    fun testBasicGetEmpty() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.queueDeclare("test", durable = true)

        val result = channel.basicGet("test")
        assertEquals(null, result.message)
        assertEquals(0u, result.messageCount)

        channel.queueDelete("test")

        channel.close()
    }

    @Test
    fun testBasicQos() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.basicQos(count = 100u, global = true)
        channel.basicQos(count = 100u, global = false)

        channel.close()
    }

}
