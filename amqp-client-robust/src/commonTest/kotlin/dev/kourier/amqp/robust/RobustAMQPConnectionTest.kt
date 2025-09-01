package dev.kourier.amqp.robust

import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.Frame
import dev.kourier.amqp.connection.amqpConfig
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class RobustAMQPConnectionTest {

    @Test
    fun testConnectionWithUrl(): Unit = runBlocking {
        val urlString = "amqp://guest:guest@localhost:5672/"
        createRobustAMQPConnection(this, urlString).close()
        createRobustAMQPConnection(this, Url(urlString)).close()
        createRobustAMQPConnection(this, amqpConfig(urlString)).close()
        createRobustAMQPConnection(this, amqpConfig(Url(urlString))).close()
    }

    @Test
    fun testConnectionDrops() = runBlocking {
        withConnection { connection ->
            val closeEvent = async { connection.closedResponses.first() }
            val reopenEvent = async { connection.openedResponses.first() }

            // Write invalid frame to close connection (heartbeat frame is only allowed on channel 0)
            connection.write(
                Frame(
                    channelId = 1u,
                    payload = Frame.Heartbeat
                )
            )

            closeEvent.await()
            reopenEvent.await()

            val channel = connection.openChannel()
            channel.close()
        }
    }

    @Test
    @OptIn(DelicateCoroutinesApi::class)
    fun testRestoreConsumeAfterConnectionDrops() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { connection.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            val queueName = "test-connection-restore-queue"
            val exchange = "test-connection-restore-exchange"
            val routingKey = "test.key"

            channel.exchangeDeclare(
                exchange,
                BuiltinExchangeType.DIRECT,
                durable = true,
                arguments = emptyMap()
            )
            channel.queueDeclare(
                queueName,
                durable = true,
                arguments = emptyMap()
            )
            channel.queueBind(queueName, exchange, routingKey, arguments = emptyMap())
            val receivedMessages = channel.basicConsume(
                queue = queueName,
                consumerTag = "restore-test-consumer",
                noAck = true,
                arguments = emptyMap()
            )
            channel.basicPublish("Before crash".toByteArray(), exchange, routingKey)

            val msg = withTimeout(5000) { receivedMessages.receive() }
            assertEquals("Before crash", msg.message.body.decodeToString())

            // Write invalid frame to close connection (heartbeat frame is only allowed on channel 0)
            connection.write(
                Frame(
                    channelId = 1u,
                    payload = Frame.Heartbeat
                )
            )
            closeEvent.await()
            reopenEvent.await()

            channel.basicPublish("After restore".toByteArray(), exchange, routingKey)

            val msg2 = withTimeout(5.seconds) { receivedMessages.receive() }
            assertEquals("After restore", msg2.message.body.decodeToString())

            channel.close()
        }
    }

    @Test
    @OptIn(DelicateCoroutinesApi::class)
    fun testRestoreConsumeNoTaAfterConnectionDrops() = runBlocking {
        withConnection { connection ->
            val channel = connection.openChannel()
            val closeEvent = async { connection.closedResponses.first() }
            val reopenEvent = async { channel.openedResponses.first() }

            val queueName = "test-connection-restore-queue"
            val exchange = "test-connection-restore-exchange"
            val routingKey = "test.key"

            channel.exchangeDeclare(
                exchange,
                BuiltinExchangeType.DIRECT,
                durable = true,
                arguments = emptyMap()
            )
            channel.queueDeclare(
                queueName,
                durable = true,
                arguments = emptyMap()
            )
            channel.queueBind(queueName, exchange, routingKey, arguments = emptyMap())
            val receivedMessages = channel.basicConsume(
                queue = queueName,
                noAck = true,
                arguments = emptyMap()
            )
            channel.basicPublish("Before crash".toByteArray(), exchange, routingKey)

            val msg = withTimeout(5000) { receivedMessages.receive() }
            assertEquals("Before crash", msg.message.body.decodeToString())

            // Write invalid frame to close connection (heartbeat frame is only allowed on channel 0)
            connection.write(
                Frame(
                    channelId = 1u,
                    payload = Frame.Heartbeat
                )
            )
            closeEvent.await()
            reopenEvent.await()

            channel.basicPublish("After restore".toByteArray(), exchange, routingKey)

            val msg2 = withTimeout(5.seconds) { receivedMessages.receive() }
            assertEquals("After restore", msg2.message.body.decodeToString())

            channel.close()
        }
    }

}
