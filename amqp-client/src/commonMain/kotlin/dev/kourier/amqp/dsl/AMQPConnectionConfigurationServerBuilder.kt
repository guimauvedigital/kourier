package dev.kourier.amqp.dsl

import dev.kourier.amqp.AMQPConnectionConfiguration
import kotlin.time.Duration

class AMQPConnectionConfigurationServerBuilder {

    var host: String = AMQPConnectionConfiguration.Server.Defaults.HOST
    var port: Int = AMQPConnectionConfiguration.Server.Defaults.PORT
    var user: String = AMQPConnectionConfiguration.Server.Defaults.USER
    var password: String = AMQPConnectionConfiguration.Server.Defaults.PASSWORD
    var vhost: String = AMQPConnectionConfiguration.Server.Defaults.VHOST
    var timeout: Duration = AMQPConnectionConfiguration.Server.Defaults.timeout
    var connectionName: String = AMQPConnectionConfiguration.Server.Defaults.CONNECTION_NAME

    fun build(): AMQPConnectionConfiguration.Server {
        return AMQPConnectionConfiguration.Server(
            host = host,
            port = port,
            user = user,
            password = password,
            vhost = vhost,
            timeout = timeout,
            connectionName = connectionName
        )
    }

}
