package dev.kourier.amqp

class AMQPChannel(
    private val connection: AMQPConnection,
    val id: ChannelId,
    val frameMax: UInt,
) {

    /**
     * Closes the channel.
     *
     * @param reason Reason that can be logged by the broker.
     * @param code Code that can be logged by the broker.
     *
     * @return Nothing. The channel is closed synchronously.
     */
    fun close(
        reason: String = "",
        code: UShort = 200u,
    ) {
        // TODO
    }

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
     * @return AMQPResponse.Channel.Exchange.Declared
     */
    suspend fun exchangeDeclare(
        name: String,
        type: String,
        passive: Boolean = false,
        durable: Boolean = false,
        autoDelete: Boolean = false,
        internal: Boolean = false,
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Exchange.Declared {
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
        return connection.writeAndWaitForResponse(declare)
    }

    /**
     * Delete an exchange.
     *
     * @param name Name of the exchange.
     * @param ifUnused If enabled, exchange will be deleted only when it is not used.
     *
     * @return AMQPResponse.Channel.Exchange.Deleted
     */
    suspend fun exchangeDelete(
        name: String,
        ifUnused: Boolean = false,
    ): AMQPResponse.Channel.Exchange.Deleted {
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
        return connection.writeAndWaitForResponse(delete)
    }

    /**
     * Bind an exchange to another exchange.
     *
     * @param destination Output exchange.
     * @param source Input exchange.
     * @param routingKey Bind only to messages matching routingKey.
     * @param arguments Bind only to messages matching given options.
     *
     * @return AMQPResponse.Channel.Exchange.Bound
     */
    suspend fun exchangeBind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Exchange.Bound {
        val bind = Frame(
            channelId = id,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Bind(
                        Frame.Method.MethodExchange.ExchangeBind(
                            reserved1 = 0u,
                            destination = destination,
                            source = source,
                            routingKey = routingKey,
                            noWait = false,
                            arguments = arguments
                        )
                    )
                )
            )
        )
        return connection.writeAndWaitForResponse(bind)
    }

    /**
     * Unbind an exchange from another exchange.
     *
     * @param destination Output exchange.
     * @param source Input exchange.
     * @param routingKey Unbind only from messages matching routingKey.
     * @param arguments Unbind only from messages matching given options.
     *
     * @return AMQPResponse.Channel.Exchange.Unbound
     */
    suspend fun exchangeUnbind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Exchange.Unbound {
        val unbind = Frame(
            channelId = id,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Unbind(
                        Frame.Method.MethodExchange.ExchangeUnbind(
                            reserved1 = 0u,
                            destination = destination,
                            source = source,
                            routingKey = routingKey,
                            noWait = false,
                            arguments = arguments
                        )
                    )
                )
            )
        )
        return connection.writeAndWaitForResponse(unbind)
    }

}
