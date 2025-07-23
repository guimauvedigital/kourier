package dev.kourier.amqp.connection

class AMQPConfigBuilder {

    var connection: AMQPConfig.Connection = AMQPConfig.Connection.Plain
    var server: AMQPConfig.Server = AMQPConfig.Server()

    fun server(block: AMQPConfigServerBuilder.() -> Unit) {
        server = AMQPConfigServerBuilder().apply(block).build()
    }

    fun build(): AMQPConfig {
        return AMQPConfig(
            connection = connection,
            server = server
        )
    }

}
