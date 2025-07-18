package dev.kourier.amqp.dsl

import dev.kourier.amqp.AMQPConnectionConfiguration

fun amqpConnectionConfiguration(block: AMQPConnectionConfigurationBuilder.() -> Unit): AMQPConnectionConfiguration {
    return AMQPConnectionConfigurationBuilder().apply(block).build()
}
