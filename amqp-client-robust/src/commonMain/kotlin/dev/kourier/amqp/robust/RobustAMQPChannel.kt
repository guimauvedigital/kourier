package dev.kourier.amqp.robust

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.ChannelId
import dev.kourier.amqp.Table
import dev.kourier.amqp.channel.DefaultAMQPChannel
import dev.kourier.amqp.connection.ConnectionState
import dev.kourier.amqp.robust.states.*

open class RobustAMQPChannel(
    override val connection: RobustAMQPConnection,
    id: ChannelId,
    frameMax: UInt,
) : DefaultAMQPChannel(connection, id, frameMax) {

    private var declaredQos: DeclaredQos? = null
    private val declaredExchanges = mutableMapOf<String, DeclaredExchange>()
    private val declaredQueues = mutableMapOf<String, DeclaredQueue>()
    private val boundExchanges = mutableMapOf<Triple<String, String, String>, BoundExchange>()
    private val boundQueues = mutableMapOf<Triple<String, String, String>, BoundQueue>()
    private val consumedQueues = mutableMapOf<Pair<String, String>, ConsumedQueue>()

    suspend fun restore() {
        open()

        declaredQos?.let {
            basicQos(
                count = it.count,
                global = it.global
            )
        }
        declaredExchanges.values.forEach {
            exchangeDeclare(
                name = it.name,
                type = it.type,
                durable = it.durable,
                autoDelete = it.autoDelete,
                internal = it.internal,
                arguments = it.arguments
            )
        }
        declaredQueues.values.forEach {
            queueDeclare(
                name = it.name,
                durable = it.durable,
                exclusive = it.exclusive,
                autoDelete = it.autoDelete,
                arguments = it.arguments
            )
        }
        boundExchanges.values.forEach {
            exchangeBind(
                destination = it.destination,
                source = it.source,
                routingKey = it.routingKey,
                arguments = it.arguments
            )
        }
        boundQueues.values.forEach {
            queueBind(
                queue = it.queue,
                exchange = it.exchange,
                routingKey = it.routingKey,
                arguments = it.arguments
            )
        }
        consumedQueues.values.forEach { consumedQueue ->
            basicConsume(
                queue = consumedQueue.queue,
                consumerTag = consumedQueue.consumerTag,
                noAck = consumedQueue.noAck,
                exclusive = consumedQueue.exclusive,
                arguments = consumedQueue.arguments,
                onDelivery = consumedQueue.onDelivery,
                onCanceled = consumedQueue.onCanceled
            )
        }
    }

    override suspend fun cancelAll(channelClosed: AMQPException.ChannelClosed) {
        if (channelClosed.isInitiatedByApplication) return super.cancelAll(channelClosed)

        if (state == ConnectionState.CLOSED) return // Already closed
        this.state = ConnectionState.CLOSED
        logger.info("Channel $id closed, attempting to restore...")
        restore()
    }

    override suspend fun basicQos(count: UShort, global: Boolean): AMQPResponse.Channel.Basic.QosOk {
        return super.basicQos(count, global).also {
            declaredQos = DeclaredQos(
                count = count,
                global = global
            )
        }
    }

    override suspend fun exchangeDeclare(
        name: String,
        type: String,
        durable: Boolean,
        autoDelete: Boolean,
        internal: Boolean,
        arguments: Table,
    ): AMQPResponse.Channel.Exchange.Declared {
        return super.exchangeDeclare(name, type, durable, autoDelete, internal, arguments).also {
            if (!internal) declaredExchanges[name] = DeclaredExchange(
                name = name,
                type = type,
                durable = durable,
                autoDelete = autoDelete,
                internal = internal,
                arguments = arguments
            )
        }
    }

    override suspend fun exchangeDelete(name: String, ifUnused: Boolean): AMQPResponse.Channel.Exchange.Deleted {
        return super.exchangeDelete(name, ifUnused).also {
            declaredExchanges.remove(name)
        }
    }

    override suspend fun exchangeBind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Exchange.Bound {
        return super.exchangeBind(destination, source, routingKey, arguments).also {
            boundExchanges[Triple(destination, source, routingKey)] = BoundExchange(
                destination = destination,
                source = source,
                routingKey = routingKey,
                arguments = arguments
            )
        }
    }

    override suspend fun exchangeUnbind(
        destination: String,
        source: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Exchange.Unbound {
        return super.exchangeUnbind(destination, source, routingKey, arguments).also {
            boundExchanges.remove(Triple(destination, source, routingKey))
        }
    }

    override suspend fun queueDeclare(
        name: String,
        durable: Boolean,
        exclusive: Boolean,
        autoDelete: Boolean,
        arguments: Table,
    ): AMQPResponse.Channel.Queue.Declared {
        return super.queueDeclare(name, durable, exclusive, autoDelete, arguments).also {
            declaredQueues[name] = DeclaredQueue(
                name = name,
                durable = durable,
                exclusive = exclusive,
                autoDelete = autoDelete,
                arguments = arguments
            )
        }
    }

    override suspend fun queueDelete(
        name: String,
        ifUnused: Boolean,
        ifEmpty: Boolean,
    ): AMQPResponse.Channel.Queue.Deleted {
        return super.queueDelete(name, ifUnused, ifEmpty).also {
            declaredQueues.remove(name)
        }
    }

    override suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Queue.Bound {
        return super.queueBind(queue, exchange, routingKey, arguments).also {
            boundQueues[Triple(queue, exchange, routingKey)] = BoundQueue(
                queue = queue,
                exchange = exchange,
                routingKey = routingKey,
                arguments = arguments
            )
        }
    }

    override suspend fun queueUnbind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Table,
    ): AMQPResponse.Channel.Queue.Unbound {
        return super.queueUnbind(queue, exchange, routingKey, arguments).also {
            boundQueues.remove(Triple(queue, exchange, routingKey))
        }
    }

    override suspend fun basicConsume(
        queue: String,
        consumerTag: String,
        noAck: Boolean,
        exclusive: Boolean,
        arguments: Table,
        onDelivery: suspend (AMQPResponse.Channel.Message.Delivery) -> Unit,
        onCanceled: suspend (AMQPResponse.Channel) -> Unit,
    ): AMQPResponse.Channel.Basic.ConsumeOk {
        return super.basicConsume(
            queue, consumerTag, noAck, exclusive, arguments, onDelivery,
            onCanceled = { response ->
                if (response is AMQPResponse.Channel.Closed && state == ConnectionState.OPEN) return@basicConsume
                onCanceled(response)
            }
        ).also {
            consumedQueues[Pair(queue, it.consumerTag)] = ConsumedQueue(
                queue = queue,
                consumerTag = consumerTag,
                noAck = noAck,
                exclusive = exclusive,
                arguments = arguments,
                onDelivery = onDelivery,
                onCanceled = onCanceled
            )
        }
    }

    override suspend fun basicCancel(consumerTag: String): AMQPResponse.Channel.Basic.Canceled {
        return super.basicCancel(consumerTag).also {
            val key = consumedQueues.entries.find { it.value.consumerTag == consumerTag }?.key ?: return@also
            consumedQueues.remove(key)
        }
    }

}
