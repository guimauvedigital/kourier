package dev.kourier.amqp.robust

import dev.kaccelero.models.UUID
import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.Field
import dev.kourier.amqp.connection.amqpConfig
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertFailsWith

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

}
