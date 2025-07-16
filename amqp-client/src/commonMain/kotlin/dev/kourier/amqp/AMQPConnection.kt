package dev.kourier.amqp

class AMQPConnection(
) {

    internal enum class ConnectionState {
        OPEN,
        SHUTTING_DOWN,
        CLOSED
    }


}
