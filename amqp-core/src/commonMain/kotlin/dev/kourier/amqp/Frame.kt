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
import dev.kourier.amqp.serialization.serializers.frame.method.queue.FrameMethodQueueSerializer
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

            data class Start(val start: ConnectionStart) : MethodConnection()
            data class StartOk(val startOk: ConnectionStartOk) : MethodConnection()
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

            data class Open(val open: ConnectionOpen) : MethodConnection()
            data class OpenOk(val reserved1: String) : MethodConnection()
            data class Close(val close: ConnectionClose) : MethodConnection()
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

            @Serializable(with = FrameMethodConnectionStartSerializer::class)
            data class ConnectionStart(
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
            )

            @Serializable(with = FrameMethodConnectionStartOkSerializer::class)
            data class ConnectionStartOk(
                val clientProperties: Table,
                val mechanism: String,
                val response: String,
                val locale: String,
            )

            @Serializable(with = FrameMethodConnectionOpenSerializer::class)
            data class ConnectionOpen(
                val vhost: String = "/",
                val reserved1: String = "",
                val reserved2: Boolean = false,
            )

            @Serializable(with = FrameMethodConnectionCloseSerializer::class)
            data class ConnectionClose(
                val replyCode: UShort,
                val replyText: String,
                val failingClassId: UShort,
                val failingMethodId: UShort,
            )

        }

        @Serializable(with = FrameMethodChannelSerializer::class)
        sealed class MethodChannel {

            data class Open(val reserved1: String) : MethodChannel()
            data class OpenOk(val reserved1: String) : MethodChannel()
            data class Flow(val active: Boolean) : MethodChannel()
            data class FlowOk(val active: Boolean) : MethodChannel()
            data class Close(val close: ChannelClose) : MethodChannel()
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

            @Serializable(with = FrameMethodChannelCloseSerializer::class)
            data class ChannelClose(
                val replyCode: UShort,
                val replyText: String,
                val classId: UShort,
                val methodId: UShort,
            )

        }

        @Serializable(with = FrameMethodExchangeSerializer::class)
        sealed class MethodExchange {

            data class Declare(val declare: ExchangeDeclare) : MethodExchange()
            object DeclareOk : MethodExchange()
            data class Delete(val delete: ExchangeDelete) : MethodExchange()
            object DeleteOk : MethodExchange()
            data class Bind(val bind: ExchangeBind) : MethodExchange()
            object BindOk : MethodExchange()
            data class Unbind(val unbind: ExchangeUnbind) : MethodExchange()
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

            @Serializable(with = FrameMethodExchangeDeclareSerializer::class)
            data class ExchangeDeclare(
                val reserved1: UShort,
                val exchangeName: String,
                val exchangeType: String,
                val passive: Boolean,
                val durable: Boolean,
                val autoDelete: Boolean,
                val internal: Boolean,
                val noWait: Boolean,
                val arguments: Table,
            )

            @Serializable(with = FrameMethodExchangeDeleteSerializer::class)
            data class ExchangeDelete(
                val reserved1: UShort,
                val exchangeName: String,
                val ifUnused: Boolean,
                val noWait: Boolean,
            )

            @Serializable(with = FrameMethodExchangeBindSerializer::class)
            data class ExchangeBind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            )

            @Serializable(with = FrameMethodExchangeUnbindSerializer::class)
            data class ExchangeUnbind(
                val reserved1: UShort,
                val destination: String,
                val source: String,
                val routingKey: String,
                val noWait: Boolean,
                val arguments: Table,
            )

        }

        @Serializable(with = FrameMethodQueueSerializer::class)
        sealed class MethodQueue {

            // TODO

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
