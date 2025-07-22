package dev.kourier.amqp.connection

enum class ConnectionState {
    OPEN,
    SHUTTING_DOWN,
    CLOSED
}
