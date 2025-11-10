package dev.kourier.tutorials

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Properties
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PublisherConfirmsTest {

    private val queueName = "test_confirms_queue"

    @Test
    fun testIndividualPublishConfirm() = runBlocking {
        // Strategy 1: Publish and wait for confirm individually
        val config = amqpConfig {
            server {
                host = "localhost"
            }
        }
        val connection = createAMQPConnection(this, config)
        val channel = connection.openChannel()

        // Declare queue
        channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = true, arguments = emptyMap())

        // Enable publisher confirms
        channel.confirmSelect()

        println("[Individual] Publishing messages...")
        val messageCount = 5

        for (i in 1..messageCount) {
            val message = "Message $i"

            // Publish message
            channel.basicPublish(
                message.toByteArray(),
                exchange = "",
                routingKey = queueName,
                properties = Properties()
            )

            // Wait for confirm
            val confirm = channel.publishConfirmResponses.first()

            when (confirm) {
                is AMQPResponse.Channel.Basic.PublishConfirm.Ack -> {
                    println(" [x] Message $i confirmed (deliveryTag: ${confirm.deliveryTag})")
                }

                is AMQPResponse.Channel.Basic.PublishConfirm.Nack -> {
                    println(" [!] Message $i nacked (deliveryTag: ${confirm.deliveryTag})")
                }
            }
        }

        println("[Individual] All messages confirmed")

        channel.close()
        connection.close()
    }

    @Test
    fun testBatchPublishConfirm() = runBlocking {
        // Strategy 2: Publish in batches, then wait for confirms
        val config = amqpConfig {
            server {
                host = "localhost"
            }
        }
        val connection = createAMQPConnection(this, config)
        val channel = connection.openChannel()

        channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = true, arguments = emptyMap())

        // Enable publisher confirms
        channel.confirmSelect()

        println("[Batch] Publishing messages in batches...")
        val batchSize = 10
        val totalMessages = 30

        var published = 0
        while (published < totalMessages) {
            // Publish a batch
            for (i in 1..batchSize) {
                val message = "Batch message ${published + i}"
                channel.basicPublish(
                    message.toByteArray(),
                    exchange = "",
                    routingKey = queueName,
                    properties = Properties()
                )
            }

            // Wait for all confirms for this batch
            val confirms = channel.publishConfirmResponses.take(batchSize).toList()

            val ackCount = confirms.count { it is AMQPResponse.Channel.Basic.PublishConfirm.Ack }
            val nackCount = confirms.count { it is AMQPResponse.Channel.Basic.PublishConfirm.Nack }

            println(" [x] Batch confirmed: $ackCount acks, $nackCount nacks")

            published += batchSize
        }

        println("[Batch] All batches confirmed")

        channel.close()
        connection.close()
    }

    @Test
    fun testAsyncPublishConfirm() = runBlocking {
        // Strategy 3: Publish and handle confirms asynchronously
        val config = amqpConfig {
            server {
                host = "localhost"
            }
        }
        val connection = createAMQPConnection(this, config)
        val channel = connection.openChannel()

        channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = true, arguments = emptyMap())

        // Enable publisher confirms
        channel.confirmSelect()

        println("[Async] Publishing messages with async confirms...")

        val messageCount = 20
        val confirmedMessages = mutableListOf<ULong>()
        val nackedMessages = mutableListOf<ULong>()

        // Launch coroutine to handle confirms asynchronously
        val confirmJob = launch {
            channel.publishConfirmResponses.take(messageCount).collect { confirm ->
                when (confirm) {
                    is AMQPResponse.Channel.Basic.PublishConfirm.Ack -> {
                        confirmedMessages.add(confirm.deliveryTag)
                        println(" [✓] Ack for delivery tag: ${confirm.deliveryTag}")
                    }

                    is AMQPResponse.Channel.Basic.PublishConfirm.Nack -> {
                        nackedMessages.add(confirm.deliveryTag)
                        println(" [✗] Nack for delivery tag: ${confirm.deliveryTag}")
                    }
                }
            }
        }

        // Publish messages without waiting
        for (i in 1..messageCount) {
            val message = "Async message $i"
            channel.basicPublish(
                message.toByteArray(),
                exchange = "",
                routingKey = queueName,
                properties = Properties()
            )
        }

        // Wait for all confirms to be processed
        confirmJob.join()

        println("[Async] Confirmed: ${confirmedMessages.size}, Nacked: ${nackedMessages.size}")
        assertEquals(messageCount, confirmedMessages.size, "All messages should be confirmed")

        channel.close()
        connection.close()
    }

    @Test
    fun testMultipleConfirms() = runBlocking {
        // Test the 'multiple' flag in confirms
        val config = amqpConfig {
            server {
                host = "localhost"
            }
        }
        val connection = createAMQPConnection(this, config)
        val channel = connection.openChannel()

        channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = true, arguments = emptyMap())

        // Enable publisher confirms
        channel.confirmSelect()

        // Publish several messages
        val messageCount = 10

        for (i in 1..messageCount) {
            channel.basicPublish(
                "Message $i".toByteArray(),
                exchange = "",
                routingKey = queueName,
                properties = Properties()
            )
        }

        // Collect confirms (might get bulk confirms with multiple=true)
        val confirms = channel.publishConfirmResponses.take(messageCount).toList()

        println("[Multiple] Received ${confirms.size} confirm responses")

        val multipleConfirms = confirms.filter { it.multiple }
        if (multipleConfirms.isNotEmpty()) {
            println("[Multiple] Received ${multipleConfirms.size} confirms with multiple=true")
        }

        // Verify all messages were confirmed
        assertTrue(confirms.all { it is AMQPResponse.Channel.Basic.PublishConfirm.Ack }, "All confirms should be acks")

        channel.close()
        connection.close()
    }

}
