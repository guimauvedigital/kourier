# kourier

[![License](https://img.shields.io/github/license/guimauvedigital/kourier)](LICENSE)
[![Maven Central Version](https://img.shields.io/maven-central/v/dev.kourier/amqp-client)](https://klibs.io/project/guimauvedigital/kourier)
[![Issues](https://img.shields.io/github/issues/guimauvedigital/kourier)]()
[![Pull Requests](https://img.shields.io/github/issues-pr/guimauvedigital/kourier)]()
[![codecov](https://codecov.io/github/guimauvedigital/kourier/branch/main/graph/badge.svg?token=F7K641TYFZ)](https://codecov.io/github/guimauvedigital/kourier)
[![CodeFactor](https://www.codefactor.io/repository/github/guimauvedigital/kourier/badge)](https://www.codefactor.io/repository/github/guimauvedigital/kourier)
[![Open Source Helpers](https://www.codetriage.com/guimauvedigital/kourier/badges/users.svg)](https://www.codetriage.com/guimauvedigital/kourier)

Pure Kotlin AMQP client and protocol implementation, optimized for KMP and coroutines.

## Installation

To use kourier, add the following to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("dev.kourier:amqp-client:0.1.0")
}
```

Make sure you have Maven Central configured:

```kotlin
repositories {
    mavenCentral()
}
```

## Usage

Here is a simple example of how to connect to an AMQP server, open a channel and so some stuff with it:

```kotlin
fun main() = runBlocking {
    val config = amqpConnectionConfiguration {
        server {
            host = "127.0.0.1"
            port = 5672
            user = "guest"
            password = "guest"
        }
    }
    val connection = AMQPConnection.connect(this, config)

    val channel = connection.openChannel()
    channel.exchangeDeclare("my-exchange", "topic")

    // More coming soon!

    connection.close()
}
```
