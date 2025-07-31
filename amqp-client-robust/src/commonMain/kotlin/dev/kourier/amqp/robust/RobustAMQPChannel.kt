package dev.kourier.amqp.robust

import dev.kourier.amqp.ChannelId
import dev.kourier.amqp.channel.DefaultAMQPChannel

open class RobustAMQPChannel(
    override val connection: RobustAMQPConnection,
    id: ChannelId,
    frameMax: UInt,
) : DefaultAMQPChannel(connection, id, frameMax) {


}
