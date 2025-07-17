package dev.kourier.amqp

import io.ktor.network.tls.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class AMQPConnectionConfiguration(
    val connection: Connection,
    val server: Server,
) {

    sealed class Connection {
        data class Tls(val tlsConfiguration: TLSConfig? = null, val sniServerName: String? = null) : Connection()
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
            const val connectionName: String = "Kourier AMQP Client"
        }

    }

}
