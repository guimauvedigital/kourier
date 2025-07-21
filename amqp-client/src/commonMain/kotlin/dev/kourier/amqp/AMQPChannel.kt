package dev.kourier.amqp

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AMQPChannel(
    private val connection: AMQPConnection,
    val id: ChannelId,
    val frameMax: UInt,
) {

    private val isConfirmMode: Boolean = false

    private val deliveryTagMutex = Mutex()
    private var deliveryTag: ULong = 0u

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
     * Publish a ByteArray message to exchange or queue.
     *
     * @param body Message payload that can be read from ByteArray.
     * @param exchange Name of exchange on which the message is published. Can be empty.
     * @param routingKey Name of routingKey that will be attached to the message.
     *        An exchange looks at the routingKey while deciding how the message has to be routed.
     *        When exchange parameter is empty routingKey is used as queueName.
     * @param mandatory When a published message cannot be routed to any queue and mandatory is true, the message will be returned to publisher.
     *        Returned message must be handled with returnListener or returnConsumer.
     *        When a published message cannot be routed to any queue and mandatory is false, the message is discarded or republished to an alternate exchange, if any.
     * @param immediate When matching queue has at least one or more consumers and immediate is set to true, message is delivered to them immediately.
     *        When matching queue has zero active consumers and immediate is set to true, message is returned to publisher.
     *        When matching queue has zero active consumers and immediate is set to false, message will be delivered to the queue.
     * @param properties Additional message properties (check amqp documentation).
     *
     * @return DeliveryTag waiting for message write to the broker.
     *         DeliveryTag is 0 when channel is not in confirm mode.
     *         DeliveryTag is > 0 (monotonically increasing) when channel is in confirm mode.
     */
    suspend fun basicPublish(
        body: ByteArray,
        exchange: String,
        routingKey: String,
        mandatory: Boolean = false,
        immediate: Boolean = false,
        properties: Properties = Properties(),
    ): AMQPResponse.Channel.Basic.Published {
        val publish = Frame.Method.Basic.Publish(
            reserved1 = 0u,
            exchange = exchange,
            routingKey = routingKey,
            mandatory = mandatory,
            immediate = immediate
        )
        val classID = publish.kind.value
        val header = Frame.Header(
            classID = classID,
            weight = 0u,
            bodySize = body.size.toULong(),
            properties = properties
        )

        val payloads = mutableListOf<Frame.Payload>()
        if (body.size <= frameMax.toInt()) {
            payloads.add(publish)
            payloads.add(header)
            payloads.add(Frame.Body(body))
        } else {
            payloads.add(publish)
            payloads.add(header)
            var offset = 0
            while (offset < body.size) {
                val length = minOf(frameMax.toInt(), body.size - offset)
                val slice = body.copyOfRange(offset, offset + length)
                payloads.add(Frame.Body(slice))
                offset += length
            }
        }

        connection.write(*payloads.map { Frame(channelId = id, payload = it) }.toTypedArray())

        return if (isConfirmMode) {
            val count = deliveryTagMutex.withLock { deliveryTag++ }
            AMQPResponse.Channel.Basic.Published(deliveryTag = count)
        } else {
            AMQPResponse.Channel.Basic.Published(deliveryTag = 0u)
        }
    }

    /**
     * Get a single message from a queue.
     *
     * @param queue Name of the queue.
     * @param noAck Controls whether message will be acked or nacked automatically (`true`) or manually (`false`).
     * @return AMQPResponse.Channel.Message.Get? when queue is not empty, otherwise null.
     * @deprecated EventLoopFuture based public API will be removed in first stable release, please use Async API.
     */
    suspend fun basicGet(
        queue: String,
        noAck: Boolean = false,
    ): AMQPResponse.Channel.Message.Get {
        val get = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Get(
                reserved1 = 0u,
                queue = queue,
                noAck = noAck
            )
        )
        return connection.writeAndWaitForResponse(get)
    }

    /**
     * Consume messages from a queue by sending them to registered consume listeners.
     *
     * @param queue Name of the queue.
     * @param consumerTag Name of the consumer; if empty, will be generated by the broker.
     * @param noAck Controls whether message will be acked or nacked automatically (`true`) or manually (`false`).
     * @param exclusive Ensures that queue can only be consumed by a single consumer.
     * @param arguments Additional arguments (check RabbitMQ documentation).
     *
     * @return AMQPResponse.Channel.Basic.ConsumeOk confirming that broker has accepted the consume request.
     */
    suspend fun basicConsume(
        queue: String,
        consumerTag: String = "",
        noAck: Boolean = false,
        exclusive: Boolean = false,
        arguments: Table = Table(emptyMap()),
    ): AMQPResponse.Channel.Basic.ConsumeOk {
        val consume = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Consume(
                reserved1 = 0u,
                queue = queue,
                consumerTag = consumerTag,
                noLocal = false,
                noAck = noAck,
                exclusive = exclusive,
                noWait = false,
                arguments = arguments
            )
        )
        return connection.writeAndWaitForResponse(consume)
    }

    /**
     * Cancel sending messages from server to consumer.
     *
     * @param consumerTag Identifier of the consumer.
     *
     * @return AMQPResponse.Channel.Basic.Canceled confirming that broker has accepted the cancel request.
     */
    suspend fun basicCancel(
        consumerTag: String,
    ): AMQPResponse.Channel.Basic.Canceled {
        // TODO: Cancel consuming (for example kotlin flows)

        val cancel = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Cancel(
                consumerTag = consumerTag,
                noWait = false
            )
        )
        return connection.writeAndWaitForResponse(cancel)
    }

    /**
     * Acknowledge a message.
     *
     * @param deliveryTag Number (identifier) of the message.
     * @param multiple Controls whether only this message is acked (`false`) or additionally all others up to it (`true`).
     */
    suspend fun basicAck(
        deliveryTag: ULong,
        multiple: Boolean = false,
    ) {
        val ack = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Ack(
                deliveryTag = deliveryTag,
                multiple = multiple
            )
        )
        return connection.write(ack)
    }

    /**
     * Acknowledge a message.
     *
     * @param message Received message.
     * @param multiple Controls whether only this message is acked (`false`) or additionally all others up to it (`true`).
     */
    suspend fun basicAck(
        message: AMQPResponse.Channel.Message.Delivery,
        multiple: Boolean = false,
    ) {
        return basicAck(message.deliveryTag, multiple)
    }

    /**
     * Reject a message.
     *
     * @param deliveryTag Number (identifier) of the message.
     * @param multiple Controls whether only this message is rejected (`false`) or additionally all others up to it (`true`).
     * @param requeue Controls whether to requeue message after reject.
     */
    suspend fun basicNack(
        deliveryTag: ULong,
        multiple: Boolean = false,
        requeue: Boolean = false,
    ) {
        val nack = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Nack(
                deliveryTag = deliveryTag,
                multiple = multiple,
                requeue = requeue
            )
        )
        return connection.write(nack)
    }

    /**
     * Reject a message.
     *
     * @param message Received message.
     * @param multiple Controls whether only this message is rejected (`false`) or additionally all others up to it (`true`).
     * @param requeue Controls whether to requeue message after reject.
     */
    suspend fun basicNack(
        message: AMQPResponse.Channel.Message.Delivery,
        multiple: Boolean = false,
        requeue: Boolean = false,
    ) {
        return basicNack(message.deliveryTag, multiple, requeue)
    }

    /**
     * Reject a message.
     *
     * @param deliveryTag Number (identifier) of the message.
     * @param requeue Controls whether to requeue message after reject.
     */
    suspend fun basicReject(
        deliveryTag: ULong,
        requeue: Boolean = false,
    ) {
        val reject = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Reject(
                deliveryTag = deliveryTag,
                requeue = requeue
            )
        )
        return connection.write(reject)
    }

    /**
     * Reject a message.
     *
     * @param message Received Message.
     * @param requeue Controls whether to requeue message after reject.
     */
    suspend fun basicReject(
        message: AMQPResponse.Channel.Message.Delivery,
        requeue: Boolean = false,
    ) {
        return basicReject(message.deliveryTag, requeue)
    }

    /**
     * Tell the broker what to do with all unacknowledged messages.
     * Unacknowledged messages retrieved by `basicGet` are requeued regardless.
     *
     * @param requeue Controls whether to requeue all messages after rejecting them.
     * @return AMQPResponse.Channel.Basic.Recovered confirming that broker has accepted the recover request.
     */
    suspend fun basicRecover(
        requeue: Boolean = false,
    ): AMQPResponse.Channel.Basic.Recovered {
        val recover = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Recover(
                requeue = requeue
            )
        )
        return connection.writeAndWaitForResponse(recover)
    }

    /**
     * Sets a prefetch limit when consuming messages.
     * No more messages will be delivered to the consumer until one or more messages have been acknowledged or rejected.
     *
     * @param count Size of the limit.
     * @param global Whether the limit will be shared across all consumers on the channel.
     *
     * @return AMQPResponse.Channel.Basic.QosOk confirming that broker has accepted the qos request.
     */
    suspend fun basicQos(
        count: UShort,
        global: Boolean = false,
    ): AMQPResponse.Channel.Basic.QosOk {
        val qos = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Qos(
                prefetchSize = 0u,
                prefetchCount = count,
                global = global
            )
        )
        return connection.writeAndWaitForResponse(qos)
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
     *
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
