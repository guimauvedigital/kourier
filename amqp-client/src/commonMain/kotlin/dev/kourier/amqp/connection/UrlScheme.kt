package dev.kourier.amqp.connection

enum class UrlScheme(val scheme: String) {

    AMQP("amqp"),
    AMQPS("amqps");

    val defaultPort: Int
        get() = when (this) {
            AMQP -> AMQPConfig.Server.Defaults.PORT
            AMQPS -> AMQPConfig.Server.Defaults.TLS_PORT
        }

}
