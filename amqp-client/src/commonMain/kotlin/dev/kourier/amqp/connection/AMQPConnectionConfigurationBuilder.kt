package dev.kourier.amqp.connection

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
