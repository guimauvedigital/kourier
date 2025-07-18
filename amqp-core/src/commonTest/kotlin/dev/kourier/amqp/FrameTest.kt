package dev.kourier.amqp

import dev.kourier.amqp.serialization.ProtocolBinary
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class FrameTest {

    @Test
    fun testFrameMethodQueueDeclare() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Declare(
                        Frame.Method.MethodQueue.QueueDeclare(
                            reserved1 = 0u,
                            queueName = "testQueue",
                            passive = false,
                            durable = true,
                            exclusive = false,
                            autoDelete = false,
                            noWait = false,
                            arguments = Table(emptyMap())
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueDeclareOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.DeclareOk(
                        Frame.Method.MethodQueue.QueueDeclareOk(
                            queueName = "testQueue",
                            messageCount = 0u,
                            consumerCount = 0u,
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueBind() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Bind(
                        Frame.Method.MethodQueue.QueueBind(
                            reserved1 = 0u,
                            queueName = "testQueue",
                            exchangeName = "testExchange",
                            routingKey = "testRoutingKey",
                            noWait = false,
                            arguments = Table(emptyMap())
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueBindOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.BindOk
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueuePurge() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Purge(
                        Frame.Method.MethodQueue.QueuePurge(
                            reserved1 = 0u,
                            queueName = "testQueue",
                            noWait = false,
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueuePurgeOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.PurgeOk(
                        messageCount = 0u,
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueDelete() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Delete(
                        Frame.Method.MethodQueue.QueueDelete(
                            reserved1 = 0u,
                            queueName = "testQueue",
                            ifUnused = false,
                            ifEmpty = false,
                            noWait = false,
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueDeleteOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.DeleteOk(
                        messageCount = 0u,
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueUnbind() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Unbind(
                        Frame.Method.MethodQueue.QueueUnbind(
                            reserved1 = 0u,
                            queueName = "testQueue",
                            exchangeName = "testExchange",
                            routingKey = "testRoutingKey",
                            arguments = Table(emptyMap())
                        )
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueUnbindOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.UnbindOk
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

}
