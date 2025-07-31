package dev.kourier.amqp.robust

import dev.kourier.amqp.connection.amqpConfig
import io.ktor.http.*
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

}
