package dev.kourier.amqp.robust

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.ChannelId
import dev.kourier.amqp.Table
import dev.kourier.amqp.channel.DefaultAMQPChannel
import dev.kourier.amqp.connection.ConnectionState
import dev.kourier.amqp.robust.declared.DeclaredExchange
import dev.kourier.amqp.robust.declared.DeclaredQos
import dev.kourier.amqp.robust.declared.DeclaredQueue

open class RobustAMQPChannel(
    override val connection: RobustAMQPConnection,
    id: ChannelId,
    frameMax: UInt,
) : DefaultAMQPChannel(connection, id, frameMax) {

    private var declaredQos: DeclaredQos? = null
    private val declaredExchanges = mutableMapOf<String, DeclaredExchange>()
    private val declaredQueues = mutableMapOf<String, DeclaredQueue>()

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
    }

    override suspend fun cancelAll(channelClosed: AMQPException.ChannelClosed) {
        if (channelClosed.isInitiatedByApplication) return super.cancelAll(channelClosed)

        if (state == ConnectionState.CLOSED) return // Already closed
        this.state = ConnectionState.CLOSED
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

}
