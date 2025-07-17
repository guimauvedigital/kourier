package dev.kourier.amqp

class AMQPChannel(
    private val connection: AMQPConnection,
    val id: ChannelId,
    val frameMax: UInt,
) {

    /**
     * Declare an exchange.
     *
     * @param name Name of the exchange.
     * @param type Type of the exchange.
     * @param passive If enabled, broker will raise exception if exchange already exists.
     * @param durable If enabled, creates an exchange stored on disk; otherwise, transient.
     * @param autoDelete If enabled, exchange will be deleted when the last consumer has stopped consuming.
     * @param internal Whether the exchange cannot be directly published to by client.
     * @param arguments Additional arguments (check RabbitMQ documentation).
     *
     * @return Suspends until declare response is received.
     */
    suspend fun exchangeDeclare(
        name: String,
        type: String,
        passive: Boolean = false,
        durable: Boolean = false,
        autoDelete: Boolean = false,
        internal: Boolean = false,
        arguments: Table = Table(emptyMap()),
    ) {
        val declare = Frame(
            channelId = id,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Declare(
                        Frame.Method.MethodExchange.ExchangeDeclare(
                            reserved1 = 0u,
                            exchangeName = name,
                            exchangeType = type,
                            passive = passive,
                            durable = durable,
                            autoDelete = autoDelete,
                            internal = internal,
                            noWait = false,
                            arguments = arguments
                        )
                    )
                )
            )
        )
        connection.write(declare)
    }

    /**
     * Delete an exchange.
     *
     * @param name Name of the exchange.
     * @param ifUnused If enabled, exchange will be deleted only when it is not used.
     *
     * @return Suspends until delete response is received.
     */
    suspend fun exchangeDelete(name: String, ifUnused: Boolean = false) {
        val delete = Frame(
            channelId = id,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Delete(
                        Frame.Method.MethodExchange.ExchangeDelete(
                            reserved1 = 0u,
                            exchangeName = name,
                            ifUnused = ifUnused,
                            noWait = false
                        )
                    )
                )
            )
        )
        connection.write(delete)
    }

}
