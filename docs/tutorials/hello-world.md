---
title: Hello World
parent: Tutorials
nav_order: 11
---

# Hello World

In this part of the tutorial we'll write two programs in Kotlin with kourier; a producer that sends a single message,
and a consumer that receives messages and prints them out. It's the "Hello World" of messaging.

In the diagram below, "P" is our producer and "C" is our consumer. The box in the middle is a queue, a message buffer
that RabbitMQ keeps on behalf of the consumer.

```mermaid
flowchart LR
    P([P]) --> hello[hello]
    hello --> C([C])

style P fill:#003B5C,stroke:white,color:white
style hello fill:#A06A00,stroke:white,color:white
style C fill:#4C5C2C,stroke:white,color:white

linkStyle default stroke:white
```

# Sending

```mermaid
flowchart LR
    P([P]) --> hello[hello]

style P fill:#003B5C,stroke:white,color:white
style hello fill:#A06A00,stroke:white,color:white

linkStyle default stroke:white
```

The publisher will connect to RabbitMQ, send a single message, then exit.

Set up the send method and the queue name:

```kotlin
val queueName = "hello"

suspend fun send(coroutineScope: CoroutineScope) {

}
```

then we can create a connection to the server:

```kotlin
suspend fun send(coroutineScope: CoroutineScope) {
    val config = amqpConfig {
        server {
            host = "localhost"
        }
    }
    val connection = createAMQPConnection(coroutineScope, config)
    val channel = connection.openChannel()

    // Publishing code will go here...

    channel.close()
    connection.close()
}
```

The connection abstracts the socket connection, and takes care of protocol version negotiation and authentication and so
on for us. Here we connect to a RabbitMQ node on the local machine, hence the _localhost_. If we wanted to connect to a
node on a different machine we'd simply specify its hostname or IP address here.

Next we create a channel, which is where most of the API for getting things done resides.

To send, we must declare a queue for us to send to; then we can publish a message to the queue:

```kotlin
channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = false, arguments = emptyMap())
val message = "Hello World!"
channel.basicPublish(message.toByteArray(), exchange = "", routingKey = queueName, properties = Properties())
println("[x] Sent '$message'")
```

Declaring a queue is idempotent, it will only be created if it doesn't exist already. The message content is a byte
array, so you can encode whatever you like there.

## Receiving

That's it for our publisher. Our consumer listens for messages from RabbitMQ, so unlike the publisher which publishes a
single message, we'll keep the consumer running to listen for messages and print them out.

```mermaid
flowchart LR
    hello --> C([C])

style hello fill:#A06A00,stroke:white,color:white
style C fill:#4C5C2C,stroke:white,color:white

linkStyle default stroke:white
```

Setting up is the same as the publisher; we open a connection and a channel, and declare the queue from which we're
going to consume. Note this matches up with the queue that send publishes to.

```kotlin
suspend fun receive(coroutineScope: CoroutineScope) {
    val config = amqpConfig {
        server {
            host = "localhost"
        }
    }
    val connection = createAMQPConnection(coroutineScope, config)
    val channel = connection.openChannel()

    channel.queueDeclare(queueName, durable = false, exclusive = false, autoDelete = false, arguments = emptyMap())
    println("[*] Waiting for messages. To exit press CTRL+C")

    // Consuming code will go here...

    channel.close()
    connection.close()
}
```

Note that we declare the queue here, as well. Because we might start the consumer before the publisher, we want to make
sure the queue exists before we try to consume messages from it.

```kotlin
val consumer = channel.basicConsume(queueName, noAck = true)
for (delivery in consumer) {
    val message: String = delivery.message.body.decodeToString()
    println("[x] Received '$message'")
}
```

## Putting it all together

You can start both functions by wrapping them in a `main` function with a `runBlocking` block:

```kotlin
fun main() = runBlocking {
    val coroutineScope = this

    launch { send(coroutineScope) }
    launch { receive(coroutineScope) }

    delay(Long.MAX_VALUE) // Keep the main thread alive to allow the consumer to run
}
```
