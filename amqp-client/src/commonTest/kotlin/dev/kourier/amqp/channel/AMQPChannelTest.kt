package dev.kourier.amqp.channel

import dev.kourier.amqp.*
import io.ktor.utils.io.core.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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

        channel.exchangeDeclare("test1", BuiltinExchangeType.TOPIC)
        channel.exchangeDeclare("test2", BuiltinExchangeType.TOPIC)

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

        val body = "{}".toByteArray()
        val properties = Properties(
            contentType = "application/json",
            contentEncoding = "UTF-8",
            headers = Table(mapOf("test" to Field.LongString("test"))),
            deliveryMode = 1u,
            priority = 1u,
            correlationId = "correlationID",
            replyTo = "replyTo",
            expiration = "60000",
            messageId = "messageID",
            timestamp = 100,
            type = "type",
            userId = "guest",
            appId = "appID"
        )
        channel.basicPublish(body = body, exchange = "", routingKey = "test", properties = properties)

        val msg = channel.basicGet("test")
        assertNotNull(msg.message)

        assertEquals(0u, msg.messageCount)
        assertEquals("{}", msg.message.body.decodeToString())
        assertEquals(properties, msg.message.properties)

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

    @Test
    fun testConsumeConfirms() = withConnection { connection ->
        val channel = connection.openChannel()

        channel.queueDeclare("test", durable = true)

        val body = "{}".toByteArray()

        repeat(6) {
            channel.basicPublish(body = body, exchange = "", routingKey = "test", properties = Properties())
        }

        run {
            val msg = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicAck(msg.deliveryTag)

            val msg2 = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicAck(msg2)
        }

        run {
            val msg = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicNack(msg.deliveryTag)

            val msg2 = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicNack(msg2)
        }

        run {
            val msg = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicReject(msg.deliveryTag)

            val msg2 = channel.basicGet("test").message ?: kotlin.test.fail()
            channel.basicReject(msg2)
        }

        channel.basicRecover(requeue = true)

        channel.queueDelete("test")

        channel.close()
    }

    @Test
    fun testBasicConsumeManualCancel() = withConnection { connection ->
        val channel = connection.openChannel()
        channel.queueDeclare("test_consume", durable = true)

        val body = "{}".toByteArray()
        repeat(100) {
            channel.basicPublish(body = body, exchange = "", routingKey = "test_consume")
        }

        val deliveryChannel = channel.basicConsume(
            queue = "test_consume",
            noAck = true
        )

        var count = 0
        runCatching {
            for (delivery in deliveryChannel) {
                count++
                if (count == 100) channel.basicCancel(deliveryChannel.consumeOk.consumerTag)
            }
        }
        assertEquals(100, count)

        channel.queueDelete("test_consume")
        channel.close()
    }

    @Test
    fun testBasicConsumeManualCancelFromReceiveChannel() = withConnection { connection ->
        val channel = connection.openChannel()
        channel.queueDeclare("test_consume", durable = true)

        val body = "{}".toByteArray()
        repeat(100) {
            channel.basicPublish(body = body, exchange = "", routingKey = "test_consume")
        }

        val deliveryChannel = channel.basicConsume(
            queue = "test_consume",
            noAck = true
        )

        var count = 0
        runCatching {
            for (delivery in deliveryChannel) {
                count++
                if (count == 100) deliveryChannel.cancel()
            }
        }
        assertEquals(100, count)

        channel.queueDelete("test_consume")
        channel.close()
    }

    @Test
    fun testOpenChannelsConcurrently() = withConnection { connection ->
        val first = connection.openChannel()
        val second = connection.openChannel()

        first.close()
        second.close()
    }

    @Test
    fun testConcurrentOperationsOnChannel() = withConnection { connection ->
        repeat(1001) { run ->
            val queueName = "temp_queue_$run"
            val channel = connection.openChannel()
            channel.queueDeclare(name = queueName, durable = false, exclusive = true)

            val receiveChannel = channel.basicConsume(queue = queueName)
            channel.basicPublish(body = "baz".toByteArray(), exchange = "", routingKey = queueName)
            channel.basicConsume(queue = queueName)
            channel.basicPublish(body = "baz".toByteArray(), exchange = "", routingKey = queueName)
            channel.basicCancel(receiveChannel.consumeOk.consumerTag)
        }
    }

}
