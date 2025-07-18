package dev.kourier.amqp

import dev.kourier.amqp.serialization.ProtocolBinary
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlin.test.Test
import kotlin.test.assertEquals

class FrameTest {

    @Test
    fun testFrameMethodExchangeDeclare() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Declare(
                        reserved1 = 0u,
                        exchangeName = "testExchange",
                        exchangeType = "direct",
                        passive = false,
                        durable = true,
                        autoDelete = false,
                        internal = false,
                        noWait = false,
                        arguments = Table(emptyMap())
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodExchangeDelete() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Delete(
                        reserved1 = 0u,
                        exchangeName = "testExchange",
                        ifUnused = false,
                        noWait = false,
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodExchangeBind() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Bind(
                        reserved1 = 0u,
                        destination = "testExchangeDestination",
                        source = "testExchangeSource",
                        routingKey = "testRoutingKey",
                        noWait = false,
                        arguments = Table(emptyMap())
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodExchangeUnbind() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Exchange(
                    Frame.Method.MethodExchange.Unbind(
                        reserved1 = 0u,
                        destination = "testExchangeDestination",
                        source = "testExchangeSource",
                        routingKey = "testRoutingKey",
                        noWait = false,
                        arguments = Table(emptyMap())
                    )
                )
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueueDeclare() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Payload.Method(
                Frame.Method.Queue(
                    Frame.Method.MethodQueue.Declare(
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
                        queueName = "testQueue",
                        messageCount = 0u,
                        consumerCount = 0u,
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
                        reserved1 = 0u,
                        queueName = "testQueue",
                        noWait = false,
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
                        reserved1 = 0u,
                        queueName = "testQueue",
                        ifUnused = false,
                        ifEmpty = false,
                        noWait = false,
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
                        reserved1 = 0u,
                        queueName = "testQueue",
                        exchangeName = "testExchange",
                        routingKey = "testRoutingKey",
                        arguments = Table(emptyMap())
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
