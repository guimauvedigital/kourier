package dev.kourier.amqp

sealed class AMQPResponse {

    sealed class Channel : AMQPResponse() {

        data class Opened(val channelId: ChannelId) : Channel()
        data class Closed(val channelId: ChannelId) : Channel()

        sealed class Message : Channel() {

            data class Delivery(val delivery: MessageDelivery) : Message()
            data class Get(val get: MessageGet? = null) : Message()
            data class Return(val returnValue: MessageReturn) : Message()

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

        sealed class Queue : Channel() {

            data class Declared(
                val queueName: String,
                val messageCount: UInt,
                val consumerCount: UInt,
            ) : Queue()

            data object Bound : Queue()
            data class Purged(val messageCount: UInt) : Queue()
            data class Deleted(val messageCount: UInt) : Queue()
            data object Unbound : Queue()

        }

        sealed class Exchange : Channel() {

            data object Declared : Exchange()
            data object Deleted : Exchange()
            data object Bound : Exchange()
            data object Unbound : Exchange()

        }

        sealed class Basic : Channel() {

            data object Recovered : Basic()
            data object QosOk : Basic()
            data class ConsumeOk(val consumerTag: String) : Basic()
            data class Canceled(val consumerTag: String) : Basic()

            sealed class PublishConfirm : Basic() {

                data class Ack(val deliveryTag: ULong, val multiple: Boolean) : PublishConfirm()
                data class Nack(val deliveryTag: ULong, val multiple: Boolean) : PublishConfirm()

            }

            data class Published(val deliveryTag: ULong) : Basic()

        }

        sealed class Confirm : Channel() {

            data object Selected : Confirm()

        }

        sealed class Tx : Channel() {

            data object Selected : Tx()
            data object Committed : Tx()
            data object Rollbacked : Tx()

        }

        data class Flowed(val active: Boolean) : Channel()

    }

    sealed class Connection : AMQPResponse() {

        data class Connected(val channelMax: UShort, val frameMax: UInt) : Connection()
        data object Closed : Connection()

    }

}
