package dev.kourier.amqp.connection

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Frame
import dev.kourier.amqp.InternalAmqpApi
import dev.kourier.amqp.channel.AMQPChannel

interface AMQPConnection {

    @InternalAmqpApi
    suspend fun write(bytes: ByteArray)

    @InternalAmqpApi
    suspend fun write(vararg frames: Frame)

    @InternalAmqpApi
    suspend fun <T : AMQPResponse> writeAndWaitForResponse(vararg frames: Frame): T

    /**
     * Opens a new channel.
     *
     * Can be used only when the connection is connected.
     * The channel ID is automatically assigned (next free one).
     *
     * @return the opened [AMQPChannel]
     */
    suspend fun openChannel(): AMQPChannel

    /**
     * Sends a heartbeat frame.
     */
    suspend fun sendHeartbeat()

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
    )

}
