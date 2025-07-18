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
     * Declares a queue.
     *
     * @param name Name of the queue.
     * @param passive If enabled, broker will raise exception if queue already exists.
     * @param durable If enabled, creates a queue stored on disk; otherwise, transient.
     * @param exclusive If enabled, queue will be deleted when the channel is closed.
     * @param autoDelete If enabled, queue will be deleted when the last consumer has stopped consuming.
     * @param arguments Additional arguments (check RabbitMQ documentation).
     *
     * @return AMQPResponse.Channel.Queue.Declared confirming that broker has accepted the request.
     */
    suspend fun queueDeclare(
        name: String,
        passive: Boolean = false,
        durable: Boolean = false,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Queue.Declared {
        val declare = Frame(
            channelId = id,
            payload = Frame.Method.Queue.Declare(
                reserved1 = 0u,
                queueName = name,
                passive = passive,
                durable = durable,
                exclusive = exclusive,
                autoDelete = autoDelete,
                noWait = false,
                arguments = arguments
            )
        )
        return connection.writeAndWaitForResponse(declare)
    }

    /**
     * Deletes a queue.
     *
     * @param name Name of the queue.
     * @param ifUnused If enabled, queue will be deleted only when there are no consumers subscribed to it.
     * @param ifEmpty If enabled, queue will be deleted only when it's empty.
     *
     * @return AMQPResponse.Channel.Queue.Deleted confirming that broker has accepted the delete request.
     */
    suspend fun queueDelete(
        name: String,
        ifUnused: Boolean = false,
        ifEmpty: Boolean = false,
    ): AMQPResponse.Channel.Queue.Deleted {
        val delete = Frame(
            channelId = id,
            payload = Frame.Method.Queue.Delete(
                reserved1 = 0u,
                queueName = name,
                ifUnused = ifUnused,
                ifEmpty = ifEmpty,
                noWait = false
            )
        )
        return connection.writeAndWaitForResponse(delete)
    }

    /**
     * Deletes all messages from a queue.
     *
     * @param name Name of the queue.
     * @return AMQPResponse.Channel.Queue.Purged confirming that broker has accepted the delete request.
     */
    suspend fun queuePurge(
        name: String,
    ): AMQPResponse.Channel.Queue.Purged {
        val purge = Frame(
            channelId = id,
            payload = Frame.Method.Queue.Purge(
                reserved1 = 0u,
                queueName = name,
                noWait = false
            )
        )
        return connection.writeAndWaitForResponse(purge)
    }

    /**
     * Binds a queue to an exchange.
     *
     * @param queue Name of the queue.
     * @param exchange Name of the exchange.
     * @param routingKey Bind only to messages matching routingKey.
     * @param arguments Bind only to messages matching given options.
     *
     * @return AMQPResponse.Channel.Queue.Bound confirming that broker has accepted the request.
     */
    suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String = "",
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Queue.Bound {
        val bind = Frame(
            channelId = id,
            payload = Frame.Method.Queue.Bind(
                reserved1 = 0u,
                queueName = queue,
                exchangeName = exchange,
                routingKey = routingKey,
                noWait = false,
                arguments = arguments
            )
        )
        return connection.writeAndWaitForResponse(bind)
    }

    /**
     * Unbinds a queue from an exchange.
     *
     * @param queue Name of the queue.
     * @param exchange Name of the exchange.
     * @param routingKey Unbind only from messages matching routingKey.
     * @param arguments Unbind only from messages matching given options.
     *
     * @return AMQPResponse.Channel.Queue.Unbound confirming that broker has accepted the unbind request.
     */
    suspend fun queueUnbind(
        queue: String,
        exchange: String,
        routingKey: String = "",
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Queue.Unbound {
        val unbind = Frame(
            channelId = id,
            payload = Frame.Method.Queue.Unbind(
                reserved1 = 0u,
                queueName = queue,
                exchangeName = exchange,
                routingKey = routingKey,
                arguments = arguments
            )
        )
        return connection.writeAndWaitForResponse(unbind)
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
            payload = Frame.Method.Exchange.Declare(
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
            payload = Frame.Method.Exchange.Delete(
                reserved1 = 0u,
                exchangeName = name,
                ifUnused = ifUnused,
                noWait = false
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
            payload = Frame.Method.Exchange.Bind(
                reserved1 = 0u,
                destination = destination,
                source = source,
                routingKey = routingKey,
                noWait = false,
                arguments = arguments
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
            payload = Frame.Method.Exchange.Unbind(
                reserved1 = 0u,
                destination = destination,
                source = source,
                routingKey = routingKey,
                noWait = false,
                arguments = arguments
            )
        )
        return connection.writeAndWaitForResponse(unbind)
    }

}
