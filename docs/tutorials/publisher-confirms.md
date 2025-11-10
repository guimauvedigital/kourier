---
layout: default
title: Publisher Confirms
parent: Tutorials
nav_order: 17
---

# Publisher Confirms

In previous tutorials we have covered various messaging patterns. However, we haven't discussed how to ensure that
messages are reliably delivered to the broker. Publisher confirms are a RabbitMQ extension to implement reliable
publishing. When publisher confirms are enabled on a channel, messages published by the client are confirmed
asynchronously by the broker, meaning they have been taken care of on the server side.

```mermaid
sequenceDiagram
    participant Publisher
    participant Channel
    participant Broker

    Publisher->>Channel: confirmSelect()
    Publisher->>Channel: basicPublish(msg1)
    Channel->>Broker: Deliver msg1
    Broker-->>Channel: Ack(deliveryTag=1)
    Channel-->>Publisher: PublishConfirm.Ack

    Publisher->>Channel: basicPublish(msg2)
    Channel->>Broker: Deliver msg2
    Broker-->>Channel: Ack(deliveryTag=2)
    Channel-->>Publisher: PublishConfirm.Ack
```

## Enabling publisher confirms on a channel

To enable publisher confirms on a channel, use the `confirmSelect` method:

```kotlin
channel.confirmSelect()
```

Once confirms are enabled on a channel, each published message receives a confirmation (ack or nack) from the broker.

## Confirm responses

Kourier provides confirms through a Flow:

```kotlin
channel.publishConfirmResponses.collect { confirm ->
    when (confirm) {
        is AMQPResponse.Channel.Basic.PublishConfirm.Ack -> {
            // Message accepted
            println("Confirmed: ${confirm.deliveryTag}")
        }
        is AMQPResponse.Channel.Basic.PublishConfirm.Nack -> {
            // Message rejected
            println("Rejected: ${confirm.deliveryTag}")
        }
    }
}
```

Each published message is assigned a delivery tag (sequence number). Confirms reference this tag to identify which
message was confirmed. The `multiple` property indicates whether the confirm applies to only the specific delivery tag
(`false`) or all messages up to and including the delivery tag (`true`).

## Strategy 1: Publishing messages individually

The simplest approach: publish a message and wait for its confirm before publishing the next.

```kotlin
suspend fun publishWithIndividualConfirms(channel: AMQPChannel, messages: List<String>) {
    // Enable publisher confirms
    channel.confirmSelect()

    for (message in messages) {
        // Publish message
        channel.basicPublish(
            message.toByteArray(),
            exchange = "",
            routingKey = "my_queue",
            properties = Properties()
        )

        // Wait for confirm
        val confirm = channel.publishConfirmResponses.first()

        when (confirm) {
            is AMQPResponse.Channel.Basic.PublishConfirm.Ack -> {
                println("✓ Message confirmed: $message")
            }
            is AMQPResponse.Channel.Basic.PublishConfirm.Nack -> {
                println("✗ Message rejected: $message")
                // Handle rejection (retry, log, etc.)
            }
        }
    }
}
```

**Pros:**

- Simple and straightforward
- Easy error handling
- Know immediately if a message failed

**Cons:**

- Very slow (hundreds of messages/second)
- Blocks on each message
- Not suitable for high throughput

## Strategy 2: Publishing messages in batches

Publish multiple messages, then wait for all confirms.

```kotlin
suspend fun publishWithBatchConfirms(channel: AMQPChannel, messages: List<String>, batchSize: Int) {
    channel.confirmSelect()

    messages.chunked(batchSize).forEach { batch ->
        // Publish entire batch
        batch.forEach { message ->
            channel.basicPublish(
                message.toByteArray(),
                exchange = "",
                routingKey = "my_queue",
                properties = Properties()
            )
        }

        // Wait for all confirms for this batch
        val confirms = channel.publishConfirmResponses.take(batch.size).toList()

        val ackCount = confirms.count { it is AMQPResponse.Channel.Basic.PublishConfirm.Ack }
        val nackCount = confirms.count { it is AMQPResponse.Channel.Basic.PublishConfirm.Nack }

        println("Batch complete: $ackCount acks, $nackCount nacks")

        if (nackCount > 0) {
            // Handle failures (can't identify specific messages easily)
            println("Warning: Some messages in batch were rejected")
        }
    }
}
```

**Pros:**

- Much faster than individual (20-30x improvement)
- Still relatively simple
- Good balance of throughput and simplicity

**Cons:**

- Hard to identify which specific message failed
- If one fails, must retry entire batch
- Still blocks between batches

## Strategy 3: Handling publisher confirms asynchronously

Handle confirms asynchronously while continuing to publish.

```kotlin
suspend fun publishWithAsyncConfirms(channel: AMQPChannel, messages: List<String>) {
    channel.confirmSelect()

    val outstandingConfirms = mutableMapOf<ULong, String>()
    var nextDeliveryTag = 1UL

    // Launch coroutine to handle confirms
    val confirmJob = launch {
        channel.publishConfirmResponses.collect { confirm ->
            when (confirm) {
                is AMQPResponse.Channel.Basic.PublishConfirm.Ack -> {
                    if (confirm.multiple) {
                        // Remove all up to and including this tag
                        outstandingConfirms.keys.filter { it <= confirm.deliveryTag }
                            .forEach { outstandingConfirms.remove(it) }
                    } else {
                        outstandingConfirms.remove(confirm.deliveryTag)
                    }
                }
                is AMQPResponse.Channel.Basic.PublishConfirm.Nack -> {
                    val message = outstandingConfirms[confirm.deliveryTag]
                    println("✗ Message nacked: $message")
                    // Handle specific message rejection
                    outstandingConfirms.remove(confirm.deliveryTag)
                }
            }
        }
    }

    // Publish all messages
    messages.forEach { message ->
        outstandingConfirms[nextDeliveryTag] = message

        channel.basicPublish(
            message.toByteArray(),
            exchange = "",
            routingKey = "my_queue",
            properties = Properties()
        )

        nextDeliveryTag++
    }

    // Wait until all confirms are received
    while (outstandingConfirms.isNotEmpty()) {
        delay(10)
    }

    confirmJob.cancel()
}
```

**Pros:**

- Best performance (only slightly slower than batch)
- Can identify specific failed messages
- Non-blocking publishing
- Production-ready

**Cons:**

- Most complex implementation
- Requires tracking outstanding messages
- Need to handle bulk confirms (`multiple` flag)

## Summary

We have covered three approaches to publisher confirms:

1. Publishing messages individually: simple, synchronous, slow
2. Publishing messages in batches: improved throughput, still synchronous
3. Handling publisher confirms asynchronously: best performance, most complex

## Putting it all together

```kotlin
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Properties
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

fun main() = runBlocking {
    val config = amqpConfig {
        server {
            host = "localhost"
        }
    }
    val connection = createAMQPConnection(this, config)
    val channel = connection.openChannel()

    // Declare queue
    channel.queueDeclare("confirms_queue", false, false, true, emptyMap())

    // Enable confirms
    channel.confirmSelect()

    // Publish with async confirms
    val messages = List(1000) { "Message $it" }

    val startTime = System.currentTimeMillis()
    publishWithAsyncConfirms(channel, messages)
    val duration = System.currentTimeMillis() - startTime

    println("Published ${messages.size} messages in ${duration}ms")

    channel.close()
    connection.close()
}
```
