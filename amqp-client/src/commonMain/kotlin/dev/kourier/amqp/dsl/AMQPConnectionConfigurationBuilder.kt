package dev.kourier.amqp.dsl

import dev.kourier.amqp.AMQPConnectionConfiguration

class AMQPConnectionConfigurationBuilder {

    var connection: AMQPConnectionConfiguration.Connection = AMQPConnectionConfiguration.Connection.Plain
    var server: AMQPConnectionConfiguration.Server = AMQPConnectionConfiguration.Server()

    fun server(block: AMQPConnectionConfigurationServerBuilder.() -> Unit) {
        server = AMQPConnectionConfigurationServerBuilder().apply(block).build()
    }

    fun build(): AMQPConnectionConfiguration {
        return AMQPConnectionConfiguration(
            connection = connection,
            server = server
        )
    }

}
