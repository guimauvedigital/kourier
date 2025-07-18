package dev.kourier.amqp

import dev.kourier.amqp.serialization.serializers.frame.FrameHeaderSerializer
import dev.kourier.amqp.serialization.serializers.frame.FrameSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.FrameMethodSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.basic.FrameMethodBasicSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.channel.FrameMethodChannelCloseSerializer
import dev.kourier.amqp.serialization.serializers.frame.method.channel.FrameMethodChannelSerializer
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

    sealed class Payload {
        data class Method(val method: Frame.Method) : Payload()
        data class Header(val header: Frame.Header) : Payload()
        data class Body(val body: ByteArray) : Payload()
        object Heartbeat : Payload()
    }

    val kind: Kind
        get() = when (payload) {
            is Payload.Method -> Kind.METHOD
            is Payload.Header -> Kind.HEADER
            is Payload.Body -> Kind.BODY
            is Payload.Heartbeat -> Kind.HEARTBEAT
        }

    enum class Kind(val value: UByte) {
        METHOD(1u),
        HEADER(2u),
        BODY(3u),
        HEARTBEAT(8u)
    }

    @Serializable(with = FrameHeaderSerializer::class)
    data class Header(
        val classID: UShort,
        val weight: UShort,
        val bodySize: ULong,
        val properties: Properties,
    )

    @Serializable(with = FrameMethodSerializer::class)
    sealed class Method {

        data class Connection(val connection: MethodConnection) : Method()
        data class Channel(val channel: MethodChannel) : Method()
        data class Exchange(val exchange: MethodExchange) : Method()
        data class Queue(val queue: MethodQueue) : Method()
        data class Basic(val basic: MethodBasic) : Method()
        data class Confirm(val confirm: MethodConfirm) : Method()
        data class Tx(val tx: MethodTx) : Method()

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
        sealed class MethodConnection {

            @Serializable(with = FrameMethodConnectionStartSerializer::class)
            data class Start(
                val versionMajor: UByte = 0u,
                val versionMinor: UByte = 9u,
                val serverProperties: Table = Table(
                    mapOf(
                        "capabilities" to Field.Table(
                            Table(
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
                        )
                    )
                ),
                val mechanisms: String = "AMQPLAIN PLAIN",
                val locales: String = "en_US",
            ) : MethodConnection()

            @Serializable(with = FrameMethodConnectionStartOkSerializer::class)
            data class StartOk(
                val clientProperties: Table,
                val mechanism: String,
                val response: String,
                val locale: String,
            ) : MethodConnection()

            data class Secure(val challenge: String) : MethodConnection()
            data class SecureOk(val response: String) : MethodConnection()
            data class Tune(
                val channelMax: UShort = 0u,
                val frameMax: UInt = 131072u,
                val heartbeat: UShort = 0u,
            ) : MethodConnection()

            data class TuneOk(
                val channelMax: UShort = 0u,
                val frameMax: UInt = 131072u,
                val heartbeat: UShort = 60u,
            ) : MethodConnection()

            @Serializable(with = FrameMethodConnectionOpenSerializer::class)
            data class Open(
                val vhost: String = "/",
                val reserved1: String = "",
                val reserved2: Boolean = false,
            ) : MethodConnection()

            data class OpenOk(val reserved1: String) : MethodConnection()

            @Serializable(with = FrameMethodConnectionCloseSerializer::class)
            data class Close(
                val replyCode: UShort,
                val replyText: String,
                val failingClassId: UShort,
                val failingMethodId: UShort,
            ) : MethodConnection()

            object CloseOk : MethodConnection()
            data class Blocked(val reason: String) : MethodConnection()
            object Unblocked : MethodConnection()

            val kind: Kind
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

        }

        @Serializable(with = FrameMethodChannelSerializer::class)
        sealed class MethodChannel {

            data class Open(val reserved1: String) : MethodChannel()
            data class OpenOk(val reserved1: String) : MethodChannel()
            data class Flow(val active: Boolean) : MethodChannel()
            data class FlowOk(val active: Boolean) : MethodChannel()

            @Serializable(with = FrameMethodChannelCloseSerializer::class)
            data class Close(
                val replyCode: UShort,
                val replyText: String,
                val classId: UShort,
                val methodId: UShort,
            ) : MethodChannel()

            object CloseOk : MethodChannel()

            val kind: Kind
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

        }

        @Serializable(with = FrameMethodExchangeSerializer::class)
        sealed class MethodExchange {

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
            ) : MethodExchange()

            object DeclareOk : MethodExchange()

            @Serializable(with = FrameMethodExchangeDeleteSerializer::class)
            data class Delete(
                val reserved1: UShort,
                val exchangeName: String,
                val ifUnused: Boolean,
                val noWait: Boolean,
            ) : MethodExchange()

            object DeleteOk : MethodExchange()

            @Serializable(with = FrameMethodExchangeBindSerializer::class)
            data class Bind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : MethodExchange()

            object BindOk : MethodExchange()

            @Serializable(with = FrameMethodExchangeUnbindSerializer::class)
            data class Unbind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : MethodExchange()

            object UnbindOk : MethodExchange()

            val kind: Kind
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

        }

        @Serializable(with = FrameMethodQueueSerializer::class)
        sealed class MethodQueue {

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
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueueDeclareOkSerializer::class)
            data class DeclareOk(
                val queueName: String,
                val messageCount: UInt,
                val consumerCount: UInt,
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueueBindSerializer::class)
            data class Bind(
                val reserved1: UShort,
                val queueName: String,
                val exchangeName: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            ) : MethodQueue()

            data object BindOk : MethodQueue()

            @Serializable(with = FrameMethodQueuePurgeSerializer::class)
            data class Purge(
                val reserved1: UShort,
                val queueName: String,
                val noWait: Boolean,
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueuePurgeOkSerializer::class)
            data class PurgeOk(
                val messageCount: UInt,
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueueDeleteSerializer::class)
            data class Delete(
                val reserved1: UShort,
                val queueName: String,
                val ifUnused: Boolean,
                val ifEmpty: Boolean,
                val noWait: Boolean,
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueueDeleteOkSerializer::class)
            data class DeleteOk(
                val messageCount: UInt,
            ) : MethodQueue()

            @Serializable(with = FrameMethodQueueUnbindSerializer::class)
            data class Unbind(
                val reserved1: UShort,
                val queueName: String,
                val exchangeName: String,
                val routingKey: String,
                val arguments: Table,
            ) : MethodQueue()

            data object UnbindOk : MethodQueue()

            val kind: Kind
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

        }

        @Serializable(with = FrameMethodBasicSerializer::class)
        sealed class MethodBasic {

            // TODO

        }

        @Serializable(with = FrameMethodConfirmSerializer::class)
        sealed class MethodConfirm {

            // TODO

        }

        @Serializable(with = FrameMethodTxSerializer::class)
        sealed class MethodTx {

            // TODO

        }

    }

}
