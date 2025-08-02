package dev.kourier.amqp.robust

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.BuiltinExchangeType
import io.ktor.utils.io.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class RobustAMQPChannelTest {

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
            assertFailsWith<AMQPException.ChannelClosed> {
                channel.exchangeDeclare(
                    "will-fail",
                    "nonexistent-type",
                    durable = true,
                    autoDelete = false,
                    internal = false,
                    arguments = emptyMap()
                )
            }
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

}
