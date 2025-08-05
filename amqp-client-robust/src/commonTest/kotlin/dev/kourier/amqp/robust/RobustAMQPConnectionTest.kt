package dev.kourier.amqp.robust

import dev.kourier.amqp.Frame
import dev.kourier.amqp.connection.amqpConfig
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

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

}
