package dev.kourier.amqp.channel

import dev.kourier.amqp.*
import dev.kourier.amqp.connection.AMQPConnection
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

open class DefaultAMQPChannel(
    private val connection: AMQPConnection,
    override val id: ChannelId,
    val frameMax: UInt,
) : AMQPChannel {

    private val isConfirmMode: Boolean = false

    private val deliveryTagMutex = Mutex()
    private var deliveryTag: ULong = 0u

    var nextMessage: PartialDelivery? = null

    override fun close(
        reason: String,
        code: UShort,
    ) {
        // TODO
    }

    override suspend fun basicPublish(
        body: ByteArray,
        exchange: String,
        routingKey: String,
        mandatory: Boolean,
        immediate: Boolean,
        properties: Properties,
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

    override suspend fun basicGet(
        queue: String,
        noAck: Boolean,
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

    override suspend fun basicConsume(
        queue: String,
        consumerTag: String,
        noAck: Boolean,
        exclusive: Boolean,
        arguments: Table,
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

    override suspend fun basicCancel(
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

    override suspend fun basicAck(
        deliveryTag: ULong,
        multiple: Boolean,
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

    override suspend fun basicAck(
        message: AMQPResponse.Channel.Message.Delivery,
        multiple: Boolean,
    ) {
        return basicAck(message.deliveryTag, multiple)
    }

    override suspend fun basicNack(
        deliveryTag: ULong,
        multiple: Boolean,
        requeue: Boolean,
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

    override suspend fun basicNack(
        message: AMQPResponse.Channel.Message.Delivery,
        multiple: Boolean,
        requeue: Boolean,
    ) {
        return basicNack(message.deliveryTag, multiple, requeue)
    }

    override suspend fun basicReject(
        deliveryTag: ULong,
        requeue: Boolean,
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

    override suspend fun basicReject(
        message: AMQPResponse.Channel.Message.Delivery,
        requeue: Boolean,
    ) {
        return basicReject(message.deliveryTag, requeue)
    }

    override suspend fun basicRecover(
        requeue: Boolean,
    ): AMQPResponse.Channel.Basic.Recovered {
        val recover = Frame(
            channelId = id,
            payload = Frame.Method.Basic.Recover(
                requeue = requeue
            )
        )
        return connection.writeAndWaitForResponse(recover)
    }

    override suspend fun basicQos(
        count: UShort,
        global: Boolean,
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

    override suspend fun queueDeclare(
        name: String,
        passive: Boolean,
        durable: Boolean,
        exclusive: Boolean,
        autoDelete: Boolean,
        arguments: Table,
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

    override suspend fun queueDelete(
        name: String,
        ifUnused: Boolean,
        ifEmpty: Boolean,
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

    override suspend fun queuePurge(
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

    override suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Table,
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

    override suspend fun queueUnbind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Table,
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

    override suspend fun exchangeDeclare(
        name: String,
        type: String,
        passive: Boolean,
        durable: Boolean,
        autoDelete: Boolean,
        internal: Boolean,
        arguments: Table,
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

    override suspend fun exchangeDelete(
        name: String,
        ifUnused: Boolean,
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

    override suspend fun exchangeBind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table,
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

    override suspend fun exchangeUnbind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table,
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
