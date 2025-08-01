package dev.kourier.amqp.robust

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.ChannelId
import dev.kourier.amqp.channel.DefaultAMQPChannel
import dev.kourier.amqp.connection.ConnectionState

open class RobustAMQPChannel(
    override val connection: RobustAMQPConnection,
    id: ChannelId,
    frameMax: UInt,
) : DefaultAMQPChannel(connection, id, frameMax) {

    suspend fun restore() {
        open()

    }

    override suspend fun cancelAll(channelClosed: AMQPException.ChannelClosed) {
        if (channelClosed.isInitiatedByApplication) return super.cancelAll(channelClosed)

        if (state == ConnectionState.CLOSED) return // Already closed
        this.state = ConnectionState.CLOSED
        restore()
    }

}
