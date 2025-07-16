package dev.kourier.amqp

sealed class AMQPResponse {

    data class Channel(val channel: ChannelResponse) : AMQPResponse()
    data class Connection(val connection: ConnectionResponse) : AMQPResponse()

    sealed class ChannelResponse {

        data class Opened(val channelId: ChannelId) : ChannelResponse()
        data class Closed(val channelId: ChannelId) : ChannelResponse()
        data class Message(val message: ChannelMessage) : ChannelResponse()
        data class Queue(val queue: ChannelQueue) : ChannelResponse()
        data class Exchange(val exchange: ChannelExchange) : ChannelResponse()
        data class Basic(val basic: ChannelBasic) : ChannelResponse()
        data class Confirm(val confirm: ChannelConfirm) : ChannelResponse()
        data class Tx(val tx: ChannelTx) : ChannelResponse()
        data class Flowed(val flowed: ChannelFlowed) : ChannelResponse()

        sealed class ChannelQueue {

            data class Declared(
                val queueName: String,
                val messageCount: UInt,
                val consumerCount: UInt,
            ) : ChannelQueue()

            data object Binded : ChannelQueue()
            data class Purged(val messageCount: UInt) : ChannelQueue()
            data class Deleted(val messageCount: UInt) : ChannelQueue()
            data object Unbinded : ChannelQueue()

        }

        sealed class ChannelExchange {

            data object Declared : ChannelExchange()
            data object Deleted : ChannelExchange()
            data object Binded : ChannelExchange()
            data object Unbinded : ChannelExchange()

        }

        sealed class ChannelBasic {

            data object Recovered : ChannelBasic()
            data object QosOk : ChannelBasic()
            data class ConsumeOk(val consumerTag: String) : ChannelBasic()
            data object Canceled : ChannelBasic()
            data class PublishConfirm(
                val publishConfirm: BasicPublishConfirm,
            ) : ChannelBasic()

            data class Published(
                val published: BasicPublished,
            ) : ChannelBasic()

            sealed class BasicPublishConfirm {

                data class Ack(val deliveryTag: ULong, val multiple: Boolean) : BasicPublishConfirm()
                data class Nack(val deliveryTag: ULong, val multiple: Boolean) : BasicPublishConfirm()

            }

            data class BasicPublished(val deliveryTag: ULong)

        }

        sealed class ChannelConfirm {

            data object Selected : ChannelConfirm()

        }

        sealed class ChannelTx {

            data object Selected : ChannelTx()
            data object Committed : ChannelTx()
            data object Rollbacked : ChannelTx()

        }

        data class ChannelFlowed(val active: Boolean)

        sealed class ChannelMessage {

            data class Delivery(val delivery: MessageDelivery) : ChannelMessage()
            data class Get(val get: MessageGet?) : ChannelMessage()
            data class Return(val returnValue: MessageReturn) : ChannelMessage()

            data class MessageDelivery(
                val exchange: String,
                val routingKey: String,
                val deliveryTag: ULong,
                val properties: Properties,
                val redelivered: Boolean,
                val body: ByteArray,
            )

            data class MessageGet(
                val message: MessageDelivery,
                val messageCount: UInt,
            )

            data class MessageReturn(
                val replyCode: UShort,
                val replyText: String,
                val exchange: String,
                val routingKey: String,
                val properties: Properties,
                val body: ByteArray,
            )
        }

    }

    sealed class ConnectionResponse {

        data class Connected(val channelMax: UShort, val frameMax: UInt) : ConnectionResponse()
        data object Closed : ConnectionResponse()

    }

}
