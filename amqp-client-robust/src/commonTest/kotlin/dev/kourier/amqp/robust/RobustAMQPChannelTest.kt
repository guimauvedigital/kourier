package dev.kourier.amqp.robust

import dev.kaccelero.models.UUID
import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.Field
import dev.kourier.amqp.channel.AMQPChannel
import io.ktor.utils.io.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

class RobustAMQPChannelTest {

    private suspend fun AMQPChannel.closeByBreaking() =
        assertFailsWith<AMQPException.ChannelClosed> {
            exchangeDeclare(
                "will-fail",
                "nonexistent-type",
                durable = true,
                autoDelete = false,
                internal = false,
                arguments = emptyMap()
            )
        }

    @Test
    @OptIn(DelicateCoroutinesApi::class)
    fun testDeclareAndRestoreEverything() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { channel.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            val queueName = "test-restore-queue"
            val exchange1 = "test-restore-exchange1"
            val exchange2 = "test-restore-exchange2"
            val routingKey = "test.key"

            // 1. Declare 2 exchanges
            channel.exchangeDeclare(
                exchange1,
                BuiltinExchangeType.DIRECT,
                durable = true,
                autoDelete = false,
                internal = false,
                arguments = emptyMap()
            )
            channel.exchangeDeclare(
                exchange2,
                BuiltinExchangeType.FANOUT,
                durable = true,
                autoDelete = false,
                internal = false,
                arguments = emptyMap()
            )

            // 2. Declare a queue
            channel.queueDeclare(
                queueName,
                durable = false,
                exclusive = false,
                autoDelete = true,
                arguments = emptyMap()
            )

            // 3. Bind queue to exchange1
            channel.queueBind(queueName, exchange1, routingKey, arguments = emptyMap())

            // 4. Bind exchange1 to exchange2 (fanout)
            channel.exchangeBind(exchange1, exchange2, routingKey = "", arguments = emptyMap())

            // 5. Start consumer
            val receivedMessages = channel.basicConsume(
                queue = queueName,
                consumerTag = "restore-test-consumer",
                noAck = true,
                exclusive = false,
                arguments = emptyMap()
            )

            // 6. Send a test message to exchange2
            channel.basicPublish("Before crash".toByteArray(), exchange2, routingKey)

            // 7. Assert it was received
            val msg = withTimeout(5000) { receivedMessages.receive() }
            assertEquals("Before crash", msg.message.body.decodeToString())

            // 8. Break the channel by declaring an invalid exchange type
            channel.closeByBreaking()
            closeEvent.await()
            reopenEvent.await()

            // 9. Send another message after restore
            channel.basicPublish("After restore".toByteArray(), exchange2, routingKey)

            // 10. Assert it was received again
            val msg2 = withTimeout(5.seconds) { receivedMessages.receive() }
            assertEquals("After restore", msg2.message.body.decodeToString())

            channel.close()
            assertTrue(receivedMessages.isClosedForReceive)
        }
    }

    @Test
    fun testGetQueueFail() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { channel.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            val name = "test-passive-queue-${UUID()}"
            channel.queueDeclare(name, autoDelete = true, arguments = mapOf("x-max-length" to Field.Int(1)))
            assertFailsWith<AMQPException.ChannelClosed> {
                channel.queueDeclare(name, autoDelete = true)
            }

            closeEvent.await()
            reopenEvent.await()

            assertFailsWith<AMQPException.ChannelClosed> {
                channel.queueDeclare(name, autoDelete = true)
            }
        }
    }

    @Test
    fun testDeleteExchange() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { channel.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            channel.exchangeDeclare("test-delete-exchange", BuiltinExchangeType.DIRECT, durable = true)
            channel.exchangeDeclare("test-delete-exchange-2", BuiltinExchangeType.FANOUT, durable = true)

            channel.exchangeBind("test-delete-exchange-2", "test-delete-exchange", routingKey = "")
            channel.exchangeUnbind("test-delete-exchange-2", "test-delete-exchange", routingKey = "")

            channel.exchangeDelete("test-delete-exchange")
            channel.exchangeDelete("test-delete-exchange-2")

            channel.closeByBreaking()
            closeEvent.await()
            reopenEvent.await()

            assertFailsWith<AMQPException.ChannelClosed> {
                channel.exchangeDeclarePassive("test-delete-exchange")
            }

            channel.close()
        }
    }

    @Test
    fun testDeleteQueue() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { channel.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            channel.exchangeDeclare("test-delete-queue-exchange", BuiltinExchangeType.DIRECT, durable = true)
            channel.queueDeclare(
                "test-delete-queue",
                durable = true,
                exclusive = false,
                autoDelete = false,
                arguments = emptyMap()
            )

            channel.queueBind("test-delete-queue", "test-delete-queue-exchange", routingKey = "")
            channel.queueUnbind("test-delete-queue", "test-delete-queue-exchange", routingKey = "")

            channel.exchangeDelete("test-delete-queue-exchange")
            channel.queueDelete("test-delete-queue")

            channel.closeByBreaking()
            closeEvent.await()
            reopenEvent.await()

            assertFailsWith<AMQPException.ChannelClosed> {
                channel.queueDeclarePassive("test-delete-queue")
            }

            channel.close()
        }
    }

    @Test
    @OptIn(DelicateCoroutinesApi::class)
    fun testCancelConsume() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { channel.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            val queueName = "test-cancel-consume-queue"
            channel.queueDeclare(
                queueName,
                durable = false,
                exclusive = false,
                autoDelete = true,
                arguments = emptyMap()
            )

            val consumerTag = "test-cancel-consumer"
            val receivedMessages = channel.basicConsume(
                queue = queueName,
                consumerTag = consumerTag,
                noAck = true,
                exclusive = false,
                arguments = emptyMap()
            )

            channel.closeByBreaking()
            closeEvent.await()
            reopenEvent.await()

            assertFalse(receivedMessages.isClosedForReceive)
            channel.basicCancel(consumerTag)
            assertTrue(receivedMessages.isClosedForReceive)
            channel.close()
        }
    }

}
