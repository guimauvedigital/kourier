package dev.kourier.amqp

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AMQPConnectionConfiguration(
    val connection: Connection,
    val server: Server,
) {

    sealed class Connection {
        //data class Tls(val tlsConfiguration: TLSConfiguration? = null, val sniServerName: String? = null) : Connection()
        object Plain : Connection()
    }

    data class Server(
        var host: String = Defaults.host,
        var port: Int = Defaults.port,
        var user: String = Defaults.user,
        var password: String = Defaults.password,
        var vhost: String = Defaults.vhost,
        var timeout: Duration = Defaults.timeout,
        var connectionName: String = Defaults.connectionName,
    ) {

        object Defaults {
            const val host: String = "localhost"
            const val port: Int = 5672
            const val user: String = "guest"
            const val password: String = "guest"
            const val vhost: String = "/"
            val timeout: Duration = 60.seconds
            const val connectionName: String = "RabbitMQNIO"
        }

        constructor(
            host: String? = null,
            port: Int? = null,
            user: String? = null,
            password: String? = null,
            vhost: String? = null,
            timeout: Duration? = null,
            connectionName: String? = null,
        ) : this(
            host = host ?: Defaults.host,
            port = port ?: Defaults.port,
            user = user ?: Defaults.user,
            password = password ?: if (user == null) Defaults.password else "",
            vhost = vhost ?: Defaults.vhost,
            timeout = timeout ?: Defaults.timeout,
            connectionName = connectionName ?: Defaults.connectionName
        )
    }

}
