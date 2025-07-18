package dev.kourier.amqp

import dev.kourier.amqp.handlers.FrameDecoder
import dev.kourier.amqp.serialization.ProtocolBinary
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.util.logging.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.io.IOException
import kotlinx.serialization.encodeToByteArray

class AMQPConnection private constructor(
    private val config: AMQPConnectionConfiguration,
    private val messageListeningScope: CoroutineScope,
    private val eventsBufferSize: Int,
) {

    companion object {

        /**
         * Connect to broker.
         *
         * @param coroutineScope CoroutineScope on which to connect.
         * @param config Configuration data.
         *
         * @return AMQPConnection instance.
         */
        suspend fun connect(
            coroutineScope: CoroutineScope,
            config: AMQPConnectionConfiguration,
        ): AMQPConnection {
            val amqpScope = CoroutineScope(coroutineScope.coroutineContext + SupervisorJob())
            val instance = AMQPConnection(config, amqpScope, 64)
            instance.connect()
            return instance
        }

    }

    private val logger = KtorSimpleLogger("AMQPConnection")

    private var socket: Socket? = null
    private var readChannel: ByteReadChannel? = null
    private var writeChannel: ByteWriteChannel? = null

    private var socketSubscription: Job? = null
    private var heartbeatSubscription: Job? = null

    private var channelMax: UShort = 0u
    private var frameMax: UInt = 0u

    private val allResponses = MutableSharedFlow<AMQPResponse>(extraBufferCapacity = eventsBufferSize)

    private val channels = AMQPChannels()

    private suspend fun connect() {
        if (socket != null && socket?.isActive == true) return

        val selector = SelectorManager(Dispatchers.IO)
        val tcpClient = aSocket(selector).tcp()

        socket = when (config.connection) {
            is AMQPConnectionConfiguration.Connection.Tls -> tcpClient
                .connect(config.server.host, config.server.port)
                .apply {
                    config.connection.tlsConfiguration?.let { tls(coroutineContext, it) } ?: tls(coroutineContext)
                }

            is AMQPConnectionConfiguration.Connection.Plain -> tcpClient
                .connect(config.server.host, config.server.port)
        }

        readChannel = socket?.openReadChannel()
        writeChannel = socket?.openWriteChannel(autoFlush = true)

        startListening()

        write(Protocol.PROTOCOL_START_0_9_1)
        val response = withTimeout(10_000) {
            allResponses
                .mapNotNull { (it as? AMQPResponse.Connection.Connected) }
                .first()
        }

        this.channelMax = response.channelMax
        this.frameMax = response.frameMax
    }

    private fun startListening() {
        socketSubscription?.cancel()
        heartbeatSubscription?.cancel()
        socketSubscription = messageListeningScope.launch {
            val readChannel = this@AMQPConnection.readChannel ?: return@launch
            FrameDecoder.decodeStreaming(readChannel) { frame ->
                logger.debug("Received frame: $frame")
                read(frame)
            }
        }
        heartbeatSubscription = messageListeningScope.launch {
            while (isActive) {
                delay(config.server.timeout.inWholeMilliseconds / 2)
                sendHeartbeat()
            }
        }
    }

    private suspend fun read(frame: Frame) {
        when (val payload = frame.payload) {
            is Frame.Method.Connection.Start -> {
                val clientProperties = Table(
                    mapOf(
                        "connection_name" to Field.LongString(config.server.connectionName),
                        "product" to Field.LongString("kourier-amqp-client"),
                        "platform" to Field.LongString("Kotlin"),
                        "version" to Field.LongString("0.1"),
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
                )
                val startOk = Frame(
                    channelId = frame.channelId,
                    payload = Frame.Method.Connection.StartOk(
                        clientProperties = clientProperties,
                        mechanism = "PLAIN",
                        response = "\u0000${config.server.user}\u0000${config.server.password}",
                        locale = "en_US"
                    )
                )
                write(startOk)
            }

            is Frame.Method.Connection.StartOk -> error("Unexpected StartOk frame received: $payload")

            is Frame.Method.Connection.Tune -> {
                this@AMQPConnection.channelMax = payload.channelMax
                this@AMQPConnection.frameMax = payload.frameMax
                this@AMQPConnection.channels.channelMax = payload.channelMax
                val tuneOk = Frame(
                    channelId = frame.channelId,
                    payload = Frame.Method.Connection.TuneOk(
                        channelMax = payload.channelMax,
                        frameMax = payload.frameMax,
                        heartbeat = payload.heartbeat
                    )
                )
                val open = Frame(
                    channelId = frame.channelId,
                    payload = Frame.Method.Connection.Open(
                        vhost = config.server.vhost,
                    )
                )
                write(tuneOk)
                write(open)
            }

            is Frame.Method.Connection.TuneOk -> error("Unexpected TuneOk frame received: $payload")

            is Frame.Method.Connection.Open -> error("Unexpected Open frame received: $payload")
            is Frame.Method.Connection.OpenOk -> allResponses.emit(
                AMQPResponse.Connection.Connected(
                    channelMax = channelMax,
                    frameMax = frameMax,
                )
            )

            is Frame.Method.Connection.Blocked -> TODO()
            is Frame.Method.Connection.Close -> TODO()
            is Frame.Method.Connection.CloseOk -> TODO()
            is Frame.Method.Connection.Secure -> TODO()
            is Frame.Method.Connection.SecureOk -> TODO()
            is Frame.Method.Connection.Unblocked -> TODO()

            is Frame.Method.Channel.Open -> error("Unexpected Open frame received: $payload")
            is Frame.Method.Channel.OpenOk -> allResponses.emit(
                AMQPResponse.Channel.Opened(
                    channelId = frame.channelId,
                )
            )

            is Frame.Method.Channel.Close -> TODO()
            is Frame.Method.Channel.CloseOk -> TODO()
            is Frame.Method.Channel.Flow -> TODO()
            is Frame.Method.Channel.FlowOk -> TODO()

            is Frame.Method.Queue.Declare -> error("Unexpected Declare frame received: $payload")
            is Frame.Method.Queue.DeclareOk -> allResponses.emit(
                AMQPResponse.Channel.Queue.Declared(
                    queueName = payload.queueName,
                    messageCount = payload.messageCount,
                    consumerCount = payload.consumerCount
                )
            )

            is Frame.Method.Queue.Bind -> error("Unexpected Bind frame received: $payload")
            is Frame.Method.Queue.BindOk -> allResponses.emit(
                AMQPResponse.Channel.Queue.Bound
            )

            is Frame.Method.Queue.Purge -> error("Unexpected Purge frame received: $payload")
            is Frame.Method.Queue.PurgeOk -> allResponses.emit(
                AMQPResponse.Channel.Queue.Purged(
                    messageCount = payload.messageCount
                )
            )

            is Frame.Method.Queue.Delete -> error("Unexpected Delete frame received: $payload")
            is Frame.Method.Queue.DeleteOk -> allResponses.emit(
                AMQPResponse.Channel.Queue.Deleted(
                    messageCount = payload.messageCount,
                )
            )

            is Frame.Method.Queue.Unbind -> error("Unexpected Unbind frame received: $payload")
            is Frame.Method.Queue.UnbindOk -> allResponses.emit(
                AMQPResponse.Channel.Queue.Unbound
            )

            is Frame.Method.Basic -> TODO()

            is Frame.Method.Exchange.Declare -> error("Unexpected Declare frame received: $payload")
            is Frame.Method.Exchange.DeclareOk -> allResponses.emit(
                AMQPResponse.Channel.Exchange.Declared
            )

            is Frame.Method.Exchange.Delete -> error("Unexpected Delete frame received: $payload")
            is Frame.Method.Exchange.DeleteOk -> allResponses.emit(
                AMQPResponse.Channel.Exchange.Deleted
            )

            is Frame.Method.Exchange.Bind -> error("Unexpected Bind frame received: $payload")
            is Frame.Method.Exchange.BindOk -> allResponses.emit(
                AMQPResponse.Channel.Exchange.Bound
            )

            is Frame.Method.Exchange.Unbind -> error("Unexpected Unbind frame received: $payload")
            is Frame.Method.Exchange.UnbindOk -> allResponses.emit(
                AMQPResponse.Channel.Exchange.Unbound
            )

            is Frame.Method.Confirm -> TODO()
            is Frame.Method.Tx -> TODO()

            is Frame.Header -> {

            }

            is Frame.Body -> {

            }

            is Frame.Heartbeat -> write(Frame(channelId = frame.channelId, payload = Frame.Heartbeat))
        }
    }

    @InternalAmqpApi
    suspend fun write(bytes: ByteArray) {
        val writeChannel = this.writeChannel ?: return
        writeChannel.writeByteArray(bytes)
        writeChannel.flush() // Maybe not needed since autoFlush is true?
    }

    @InternalAmqpApi
    suspend fun write(frame: Frame) {
        logger.debug("Sent frame: $frame")
        write(ProtocolBinary.encodeToByteArray(frame))
    }

    @InternalAmqpApi
    @Suppress("Unchecked_Cast")
    suspend fun <T : AMQPResponse> writeAndWaitForResponse(frame: Frame): T {
        write(frame)
        return allResponses.mapNotNull { it as? T }.first()
    }

    /**
     * Opens a new channel.
     *
     * Can be used only when the connection is connected.
     * The channel ID is automatically assigned (next free one).
     *
     * @return the opened [AMQPChannel]
     */
    suspend fun openChannel(): AMQPChannel {
        val channelId = channels.reserveNext() ?: throw AMQPConnectionError.TooManyOpenedChannels

        val channelOpen = Frame(
            channelId = channelId,
            payload = Frame.Method.Channel.Open(
                reserved1 = ""
            )
        )
        val response = writeAndWaitForResponse<AMQPResponse.Channel.Opened>(channelOpen)
        return AMQPChannel(
            connection = this,
            id = response.channelId,
            frameMax = frameMax
        ).also { channels.add(it) }
    }

    /**
     * Sends a heartbeat frame.
     */
    suspend fun sendHeartbeat() {
        write(Frame(channelId = 0u, payload = Frame.Heartbeat))
    }

    /**
     * Closes the connection.
     *
     * @param reason Reason that can be logged by the broker.
     * @param code Code that can be logged by the broker.
     *
     * @return Nothing. The connection is closed synchronously.
     */
    fun close(
        reason: String = "",
        code: UShort = 200u,
    ) {
        // TODO: Send close frame

        socketSubscription?.cancel()
        heartbeatSubscription?.cancel()
        socket?.close()
        readChannel?.cancel()
        writeChannel?.cancel(IOException())

        socketSubscription = null
        heartbeatSubscription = null
        socket = null
        readChannel = null
        writeChannel = null
    }

}
