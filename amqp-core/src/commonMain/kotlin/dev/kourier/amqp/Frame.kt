package dev.kourier.amqp

import dev.kourier.amqp.serialization.serializers.frame.FrameHeaderSerializer
import dev.kourier.amqp.serialization.serializers.frame.FrameSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.FrameMethodSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.basic.*
import dev.kourier.amqp.serialization.serializers.frame.method.channel.FrameMethodChannelCloseSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.channel.FrameMethodChannelSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.confirm.FrameMethodConfirmSelectSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.confirm.FrameMethodConfirmSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.connection.*
import dev.kourier.amqp.serialization.serializers.frame.method.exchange.*
import dev.kourier.amqp.serialization.serializers.frame.method.queue.*
import dev.kourier.amqp.serialization.serializers.frame.method.tx.FrameMethodTxSerializer
import kotlinx.serialization.Serializable

@Serializable(with = FrameSerializer::class)
data class Frame(
    val channelId: ChannelId,
    val payload: Payload,
) {

    val kind: Kind
        get() = when (payload) {
            is Method -> Kind.METHOD
            is Header -> Kind.HEADER
            is Body -> Kind.BODY
            is Heartbeat -> Kind.HEARTBEAT
        }

    enum class Kind(val value: UByte) {
        METHOD(1u),
        HEADER(2u),
        BODY(3u),
        HEARTBEAT(8u)
    }

    sealed class Payload

    @Serializable(with = FrameHeaderSerializer::class)
    data class Header(
        val classID: UShort,
        val weight: UShort,
        val bodySize: ULong,
        val properties: Properties,
    ) : Payload()

    @Serializable(with = FrameMethodSerializer::class)
    sealed class Method : Payload() {

        val kind: Kind
            get() = when (this) {
                is Connection -> Kind.CONNECTION
                is Channel -> Kind.CHANNEL
                is Exchange -> Kind.EXCHANGE
                is Queue -> Kind.QUEUE
                is Basic -> Kind.BASIC
                is Confirm -> Kind.CONFIRM
                is Tx -> Kind.TX
            }

        enum class Kind(val value: UShort) {
            CONNECTION(10u),
            CHANNEL(20u),
            EXCHANGE(40u),
            QUEUE(50u),
            BASIC(60u),
            CONFIRM(85u),
            TX(90u)
        }

        @Serializable(with = FrameMethodConnectionSerializer::class)
        sealed class Connection : Method() {

            val connectionKind: Kind
                get() = when (this) {
                    is Start -> Kind.START
                    is StartOk -> Kind.START_OK
                    is Secure -> Kind.SECURE
                    is SecureOk -> Kind.SECURE_OK
                    is Tune -> Kind.TUNE
                    is TuneOk -> Kind.TUNE_OK
                    is Open -> Kind.OPEN
                    is OpenOk -> Kind.OPEN_OK
                    is Close -> Kind.CLOSE
                    is CloseOk -> Kind.CLOSE_OK
                    is Blocked -> Kind.BLOCKED
                    is Unblocked -> Kind.UNBLOCKED
                }

            enum class Kind(val value: UShort) {
                START(10u),
                START_OK(11u),
                SECURE(20u),
                SECURE_OK(21u),
                TUNE(30u),
                TUNE_OK(31u),
                OPEN(40u),
                OPEN_OK(41u),
                CLOSE(50u),
                CLOSE_OK(51u),
                BLOCKED(60u),
                UNBLOCKED(61u)
            }

            @Serializable(with = FrameMethodConnectionStartSerializer::class)
            data class Start(
                val versionMajor: UByte = 0u,
                val versionMinor: UByte = 9u,
                val serverProperties: Table = mapOf(
                    "capabilities" to Field.Table(
                        mapOf(
                            "publisher_confirms" to Field.Boolean(true),
                            "exchange_exchange_bindings" to Field.Boolean(true),
                            "basic.nack" to Field.Boolean(true),
                            "per_consumer_qos" to Field.Boolean(true),
                            "authentication_failure_close" to Field.Boolean(true),
                            "consumer_cancel_notify" to Field.Boolean(true),
                            "connection.blocked" to Field.Boolean(true),
                        )
                    )
                ),
                val mechanisms: String = "AMQPLAIN PLAIN",
                val locales: String = "en_US",
            ) : Connection()

            @Serializable(with = FrameMethodConnectionStartOkSerializer::class)
            data class StartOk(
                val clientProperties: Table,
                val mechanism: String,
                val response: String,
                val locale: String,
            ) : Connection()

            data class Secure(
                val challenge: String,
            ) : Connection()

            data class SecureOk(
                val response: String,
            ) : Connection()

            data class Tune(
                val channelMax: UShort = 0u,
                val frameMax: UInt = 131072u,
                val heartbeat: UShort = 0u,
            ) : Connection()

            data class TuneOk(
                val channelMax: UShort = 0u,
                val frameMax: UInt = 131072u,
                val heartbeat: UShort = 60u,
            ) : Connection()

            @Serializable(with = FrameMethodConnectionOpenSerializer::class)
            data class Open(
                val vhost: String = "/",
                val reserved1: String = "",
                val reserved2: Boolean = false,
            ) : Connection()

            data class OpenOk(
                val reserved1: String,
            ) : Connection()

            @Serializable(with = FrameMethodConnectionCloseSerializer::class)
            data class Close(
                val replyCode: UShort,
                val replyText: String,
                val failingClassId: UShort,
                val failingMethodId: UShort,
            ) : Connection()

            object CloseOk : Connection()

            data class Blocked(
                val reason: String,
            ) : Connection()

            object Unblocked : Connection()

        }

        @Serializable(with = FrameMethodChannelSerializer::class)
        sealed class Channel : Method() {

            val channelKind: Kind
                get() = when (this) {
                    is Open -> Kind.OPEN
                    is OpenOk -> Kind.OPEN_OK
                    is Flow -> Kind.FLOW
                    is FlowOk -> Kind.FLOW_OK
                    is Close -> Kind.CLOSE
                    is CloseOk -> Kind.CLOSE_OK
                }

            enum class Kind(val value: UShort) {
                OPEN(10u),
                OPEN_OK(11u),
                FLOW(20u),
                FLOW_OK(21u),
                CLOSE(40u),
                CLOSE_OK(41u)
            }

            data class Open(
                val reserved1: String,
            ) : Channel()

            data class OpenOk(
                val reserved1: String,
            ) : Channel()

            data class Flow(
                val active: Boolean,
            ) : Channel()

            data class FlowOk(
                val active: Boolean,
            ) : Channel()

            @Serializable(with = FrameMethodChannelCloseSerializer::class)
            data class Close(
                val replyCode: UShort,
                val replyText: String,
                val classId: UShort,
                val methodId: UShort,
            ) : Channel()

            object CloseOk : Channel()

        }

        @Serializable(with = FrameMethodExchangeSerializer::class)
        sealed class Exchange : Method() {

            val exchangeKind: Kind
                get() = when (this) {
                    is Declare -> Kind.DECLARE
                    is DeclareOk -> Kind.DECLARE_OK
                    is Delete -> Kind.DELETE
                    is DeleteOk -> Kind.DELETE_OK
                    is Bind -> Kind.BIND
                    is BindOk -> Kind.BIND_OK
                    is Unbind -> Kind.UNBIND
                    is UnbindOk -> Kind.UNBIND_OK
                }

            enum class Kind(val value: UShort) {
                DECLARE(10u),
                DECLARE_OK(11u),
                DELETE(20u),
                DELETE_OK(21u),
                BIND(30u),
                BIND_OK(31u),
                UNBIND(40u),
                UNBIND_OK(51u)
            }

            @Serializable(with = FrameMethodExchangeDeclareSerializer::class)
            data class Declare(
                val reserved1: UShort,
                val exchangeName: String,
                val exchangeType: String,
                val passive: Boolean,
                val durable: Boolean,
                val autoDelete: Boolean,
                val internal: Boolean,
                val noWait: Boolean,
                val arguments: Table,
            ) : Exchange()

            object DeclareOk : Exchange()

            @Serializable(with = FrameMethodExchangeDeleteSerializer::class)
            data class Delete(
                val reserved1: UShort,
                val exchangeName: String,
                val ifUnused: Boolean,
                val noWait: Boolean,
            ) : Exchange()

            object DeleteOk : Exchange()

            @Serializable(with = FrameMethodExchangeBindSerializer::class)
            data class Bind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : Exchange()

            object BindOk : Exchange()

            @Serializable(with = FrameMethodExchangeUnbindSerializer::class)
            data class Unbind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : Exchange()

            object UnbindOk : Exchange()

        }

        @Serializable(with = FrameMethodQueueSerializer::class)
        sealed class Queue : Method() {

            val queueKind: Kind
                get() = when (this) {
                    is Declare -> Kind.DECLARE
                    is DeclareOk -> Kind.DECLARE_OK
                    is Bind -> Kind.BIND
                    is BindOk -> Kind.BIND_OK
                    is Purge -> Kind.PURGE
                    is PurgeOk -> Kind.PURGE_OK
                    is Delete -> Kind.DELETE
                    is DeleteOk -> Kind.DELETE_OK
                    is Unbind -> Kind.UNBIND
                    is UnbindOk -> Kind.UNBIND_OK
                }

            enum class Kind(val value: UShort) {
                DECLARE(10u),
                DECLARE_OK(11u),
                BIND(20u),
                BIND_OK(21u),
                PURGE(30u),
                PURGE_OK(31u),
                DELETE(40u),
                DELETE_OK(41u),
                UNBIND(50u),
                UNBIND_OK(51u)
            }

            @Serializable(with = FrameMethodQueueDeclareSerializer::class)
            data class Declare(
                val reserved1: UShort,
                val queueName: String,
                val passive: Boolean,
                val durable: Boolean,
                val exclusive: Boolean,
                val autoDelete: Boolean,
                val noWait: Boolean,
                val arguments: Table,
            ) : Queue()

            @Serializable(with = FrameMethodQueueDeclareOkSerializer::class)
            data class DeclareOk(
                val queueName: String,
                val messageCount: UInt,
                val consumerCount: UInt,
            ) : Queue()

            @Serializable(with = FrameMethodQueueBindSerializer::class)
            data class Bind(
                val reserved1: UShort,
                val queueName: String,
                val exchangeName: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : Queue()

            data object BindOk : Queue()

            @Serializable(with = FrameMethodQueuePurgeSerializer::class)
            data class Purge(
                val reserved1: UShort,
                val queueName: String,
                val noWait: Boolean,
            ) : Queue()

            @Serializable(with = FrameMethodQueuePurgeOkSerializer::class)
            data class PurgeOk(
                val messageCount: UInt,
            ) : Queue()

            @Serializable(with = FrameMethodQueueDeleteSerializer::class)
            data class Delete(
                val reserved1: UShort,
                val queueName: String,
                val ifUnused: Boolean,
                val ifEmpty: Boolean,
                val noWait: Boolean,
            ) : Queue()

            @Serializable(with = FrameMethodQueueDeleteOkSerializer::class)
            data class DeleteOk(
                val messageCount: UInt,
            ) : Queue()

            @Serializable(with = FrameMethodQueueUnbindSerializer::class)
            data class Unbind(
                val reserved1: UShort,
                val queueName: String,
                val exchangeName: String,
                val routingKey: String,
                val arguments: Table,
            ) : Queue()

            data object UnbindOk : Queue()

        }

        @Serializable(with = FrameMethodBasicSerializer::class)
        sealed class Basic : Method() {

            val basicKind: Kind
                get() = when (this) {
                    is Qos -> Kind.QOS
                    is QosOk -> Kind.QOS_OK
                    is Consume -> Kind.CONSUME
                    is ConsumeOk -> Kind.CONSUME_OK
                    is Cancel -> Kind.CANCEL
                    is CancelOk -> Kind.CANCEL_OK
                    is Publish -> Kind.PUBLISH
                    is Return -> Kind.RETURN
                    is Deliver -> Kind.DELIVER
                    is Get -> Kind.GET
                    is GetOk -> Kind.GET_OK
                    is GetEmpty -> Kind.GET_EMPTY
                    is Ack -> Kind.ACK
                    is Reject -> Kind.REJECT
                    is RecoverAsync -> Kind.RECOVER_ASYNC
                    is Recover -> Kind.RECOVER
                    is RecoverOk -> Kind.RECOVER_OK
                    is Nack -> Kind.NACK
                }

            enum class Kind(val value: UShort) {
                QOS(10u),
                QOS_OK(11u),
                CONSUME(20u),
                CONSUME_OK(21u),
                CANCEL(30u),
                CANCEL_OK(31u),
                PUBLISH(40u),
                RETURN(50u),
                DELIVER(60u),
                GET(70u),
                GET_OK(71u),
                GET_EMPTY(72u),
                ACK(80u),
                REJECT(90u),
                RECOVER_ASYNC(100u),
                RECOVER(110u),
                RECOVER_OK(111u),
                NACK(120u)
            }

            @Serializable(with = FrameMethodBasicQosSerializer::class)
            data class Qos(
                val prefetchSize: UInt,
                val prefetchCount: UShort,
                val global: Boolean,
            ) : Basic()

            data object QosOk : Basic()

            @Serializable(with = FrameMethodBasicConsumeSerializer::class)
            data class Consume(
                val reserved1: UShort,
                val queue: String,
                val consumerTag: String,
                val noLocal: Boolean,
                val noAck: Boolean,
                val exclusive: Boolean,
                val noWait: Boolean,
                val arguments: Table,
            ) : Basic()

            @Serializable(with = FrameMethodBasicConsumeOkSerializer::class)
            data class ConsumeOk(
                val consumerTag: String,
            ) : Basic()

            @Serializable(with = FrameMethodBasicCancelSerializer::class)
            data class Cancel(
                val consumerTag: String,
                val noWait: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicCancelOkSerializer::class)
            data class CancelOk(
                val consumerTag: String,
            ) : Basic()

            @Serializable(with = FrameMethodBasicPublishSerializer::class)
            data class Publish(
                val reserved1: UShort,
                val exchange: String,
                val routingKey: String,
                val mandatory: Boolean,
                val immediate: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicReturnSerializer::class)
            data class Return(
                val replyCode: UShort,
                val replyText: String,
                val exchange: String,
                val routingKey: String,
            ) : Basic()

            @Serializable(with = FrameMethodBasicDeliverSerializer::class)
            data class Deliver(
                val consumerTag: String,
                val deliveryTag: ULong,
                val redelivered: Boolean,
                val exchange: String,
                val routingKey: String,
            ) : Basic()

            @Serializable(with = FrameMethodBasicGetSerializer::class)
            data class Get(
                val reserved1: UShort,
                val queue: String,
                val noAck: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicGetOkSerializer::class)
            data class GetOk(
                val deliveryTag: ULong,
                val redelivered: Boolean,
                val exchange: String,
                val routingKey: String,
                val messageCount: UInt,
            ) : Basic()

            @Serializable(with = FrameMethodBasicGetEmptySerializer::class)
            data class GetEmpty(
                val reserved1: String,
            ) : Basic()

            @Serializable(with = FrameMethodBasicAckSerializer::class)
            data class Ack(
                val deliveryTag: ULong,
                val multiple: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicRejectSerializer::class)
            data class Reject(
                val deliveryTag: ULong,
                val requeue: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicRecoverAsyncSerializer::class)
            data class RecoverAsync(
                val requeue: Boolean,
            ) : Basic()

            @Serializable(with = FrameMethodBasicRecoverSerializer::class)
            data class Recover(
                val requeue: Boolean,
            ) : Basic()

            data object RecoverOk : Basic()

            @Serializable(with = FrameMethodBasicNackSerializer::class)
            data class Nack(
                val deliveryTag: ULong,
                val multiple: Boolean,
                val requeue: Boolean,
            ) : Basic()

        }

        @Serializable(with = FrameMethodConfirmSerializer::class)
        sealed class Confirm : Method() {

            val confirmKind: Kind
                get() = when (this) {
                    is Select -> Kind.SELECT
                    is SelectOk -> Kind.SELECT_OK
                }

            enum class Kind(val value: UShort) {
                SELECT(10u),
                SELECT_OK(11u)
            }

            @Serializable(with = FrameMethodConfirmSelectSerializer::class)
            data class Select(
                val noWait: Boolean,
            ) : Confirm()

            data object SelectOk : Confirm()

        }

        @Serializable(with = FrameMethodTxSerializer::class)
        sealed class Tx : Method() {

            // TODO

        }

    }

    data class Body(
        val body: ByteArray,
    ) : Payload() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Body
            return body.contentEquals(other.body)
        }

        override fun hashCode(): Int = body.contentHashCode()

    }

    object Heartbeat : Payload()

}
