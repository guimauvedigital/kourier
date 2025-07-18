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
        val host: String = Defaults.HOST,
        val port: Int = Defaults.PORT,
        val user: String = Defaults.USER,
        val password: String = Defaults.PASSWORD,
        val vhost: String = Defaults.VHOST,
        val timeout: Duration = Defaults.timeout,
        val connectionName: String = Defaults.CONNECTION_NAME,
    ) {

        object Defaults {
            const val HOST: String = "localhost"
            const val PORT: Int = 5672
            const val USER: String = "guest"
            const val PASSWORD: String = "guest"
            const val VHOST: String = "/"
            val timeout: Duration = 60.seconds
            const val CONNECTION_NAME: String = "Kourier AMQP Client"
        }

    }

}
