# kourier

[![License](https://img.shields.io/github/license/guimauvedigital/kourier)](LICENSE)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.kourier/amqp-client)](https://klibs.io/project/guimauvedigital/kourier)
[![Issues](https://img.shields.io/github/issues/guimauvedigital/kourier)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/guimauvedigital/kourier)]()
[![codecov](https://codecov.io/github/guimauvedigital/kourier/branch/main/graph/badge.svg?token=F7K641TYFZ)](https://codecov.io/github/guimauvedigital/kourier)
[![CodeFactor](https://www.codefactor.io/repository/github/guimauvedigital/kourier/badge)](https://www.codefactor.io/repository/github/guimauvedigital/kourier)
[![Open Source Helpers](https://www.codetriage.com/guimauvedigital/kourier/badges/users.svg)](https://www.codetriage.com/guimauvedigital/kourier)

Pure Kotlin AMQP/RabbitMQ client and protocol implementation, optimized for KMP and coroutines.

* **Documentation:** [kourier.dev](https://kourier.dev)
* **AI-generated wiki:** [deepwiki.com/guimauvedigital/kourier](https://deepwiki.com/guimauvedigital/kourier)
* **Repository:** [github.com/guimauvedigital/kourier](https://github.com/guimauvedigital/kourier)
* **Code coverage:** [codecov.io/github/guimauvedigital/kourier](https://codecov.io/github/guimauvedigital/kourier)

## Motivation

Why we made kourier:

* **Pure Kotlin Implementation** with no dependency on the Java client or other library.
* **Coroutines-first** design, allowing better integration with Kotlin's concurrency model and asynchronous consuming.
* **Multiplatform support** allows compatibility with JVM but also Native targets.
* **Robustness** with automatic recovery and reconnection logic, making it resilient to network issues and protocol
  errors.

## Modules

* `amqp-core`: Core AMQP 0.9.1 protocol implementation, including frames and encoding/decoding logic.
* `amqp-client`: High-level AMQP client built on top of `amqp-core`, providing connection management, channel handling,
  and basic operations like publishing and consuming messages.
* `amqp-client-robust`: Adds automatic recovery and reconnection logic to the `amqp-client`, making it more resilient to
  network issues and protocol errors, inspired by [aio-pika](https://github.com/mosquito/aio-pika)'s robust client.

Most of the time you will only need the `amqp-client` module, which depends itself on `amqp-core`, or the
`amqp-client-robust` module which depends on both `amqp-client` and `amqp-core` if you want automatic recovery features.

## Installation

To use kourier, add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.kourier:amqp-client:0.3.0")
}
```

Or if you want the robust client with automatic recovery:

```kotlin
dependencies {
    implementation("dev.kourier:amqp-client-robust:0.3.0")
}
```

Make sure you have Maven Central configured:

```kotlin
repositories {
    mavenCentral()
}
```

## Usage

Here is a simple example of how to connect to an AMQP server, open a channel and do some stuff with it:

```kotlin
fun main() = runBlocking {
    val config = amqpConfig {
        server {
            host = "127.0.0.1"
            port = 5672
            user = "guest"
            password = "guest"
        }
    }
    val connection = createAMQPConnection(this, config)

    val channel = connection.openChannel()
    channel.exchangeDeclare("my-exchange", BuiltinExchangeType.DIRECT)
    channel.queueDeclare("my-queue", durable = true)
    channel.queueBind("my-queue", "my-exchange", "my-routing-key")
    channel.basicPublish("Hello, AMQP!".toByteArray(), "my-exchange", "my-routing-key")

    val consumer = channel.basicConsume("my-queue")
    for (delivery in consumer) {
        println("Received message: ${delivery.message.body.decodeToString()}")
        delay(10_000) // Simulate processing time
        channel.basicAck(delivery.message)
    }

    channel.close()
    connection.close()
}
```

Alternative ways to connect to a broker:

```kotlin
// Configuration when connecting
val connection = createAMQPConnection(this) {
    server {
        // ... same as before
    }
}

// Configuration from URL
val config = amqpConfig("amqp://guest:guest@localhost:5672/")
val connection = createAMQPConnection(this, config)

// Directly using a connection string
val connection = createAMQPConnection(this, "amqp://guest:guest@localhost:5672/")
```

If you want to use the robust client with automatic recovery, you can use `createRobustAMQPConnection` instead of
`createAMQPConnection`. This will handle reconnections and recovery of channels and consumers automatically.

```kotlin
val connection = createRobustAMQPConnection(this, config) // All configuration options are available as before

// Do stuff with the connection as before
```

More examples can be found on the [tutorial section of the documentation](https://kourier.dev/tutorials/).

## Libraries using kourier

- [dev.kaccelero:messaging-amqp](https://github.com/guimauvedigital/kaccelero/tree/main/messaging-amqp): AMQP/RabbitMQ
  implementation of a messaging queue service.

If you are using kourier in your library, please let us know by opening a pull request to add it to this list!
