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
            payload = Frame.Method.Exchange.Declare(
                reserved1 = 0u,
                exchangeName = "testExchange",
                exchangeType = BuiltinExchangeType.DIRECT,
                passive = false,
                durable = true,
                autoDelete = false,
                internal = false,
                noWait = false,
                arguments = emptyMap()
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
            payload = Frame.Method.Exchange.Delete(
                reserved1 = 0u,
                exchangeName = "testExchange",
                ifUnused = false,
                noWait = false,
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
            payload = Frame.Method.Exchange.Bind(
                reserved1 = 0u,
                destination = "testExchangeDestination",
                source = "testExchangeSource",
                routingKey = "testRoutingKey",
                noWait = false,
                arguments = emptyMap()
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
            payload = Frame.Method.Exchange.Unbind(
                reserved1 = 0u,
                destination = "testExchangeDestination",
                source = "testExchangeSource",
                routingKey = "testRoutingKey",
                noWait = false,
                arguments = emptyMap()
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
            payload = Frame.Method.Queue.Declare(
                reserved1 = 0u,
                queueName = "testQueue",
                passive = false,
                durable = true,
                exclusive = false,
                autoDelete = false,
                noWait = false,
                arguments = emptyMap()
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
            payload = Frame.Method.Queue.DeclareOk(
                queueName = "testQueue",
                messageCount = 0u,
                consumerCount = 0u,
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
            payload = Frame.Method.Queue.Bind(
                reserved1 = 0u,
                queueName = "testQueue",
                exchangeName = "testExchange",
                routingKey = "testRoutingKey",
                noWait = false,
                arguments = emptyMap()
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
            payload = Frame.Method.Queue.BindOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodQueuePurge() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Queue.Purge(
                reserved1 = 0u,
                queueName = "testQueue",
                noWait = false,
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
            payload = Frame.Method.Queue.PurgeOk(
                messageCount = 0u,
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
            payload = Frame.Method.Queue.Delete(
                reserved1 = 0u,
                queueName = "testQueue",
                ifUnused = false,
                ifEmpty = false,
                noWait = false,
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
            payload = Frame.Method.Queue.DeleteOk(
                messageCount = 0u,
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
            payload = Frame.Method.Queue.Unbind(
                reserved1 = 0u,
                queueName = "testQueue",
                exchangeName = "testExchange",
                routingKey = "testRoutingKey",
                arguments = emptyMap()
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
            payload = Frame.Method.Queue.UnbindOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicQos() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Qos(
                prefetchSize = 0u,
                prefetchCount = 1u,
                global = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicQosOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.QosOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicConsume() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Consume(
                reserved1 = 0u,
                queue = "testQueue",
                consumerTag = "testConsumer",
                noLocal = false,
                noAck = true,
                exclusive = false,
                noWait = false,
                arguments = emptyMap()
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicConsumeOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.ConsumeOk(
                consumerTag = "testConsumer",
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicCancel() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Cancel(
                consumerTag = "testConsumer",
                noWait = false,
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicCancelOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.CancelOk(
                consumerTag = "testConsumer",
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicPublish() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Publish(
                reserved1 = 0u,
                exchange = "testExchange",
                routingKey = "testRoutingKey",
                mandatory = false,
                immediate = true,
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicReturn() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Return(
                replyCode = 200u,
                replyText = "OK",
                exchange = "testExchange",
                routingKey = "testRoutingKey",
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicDeliver() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Deliver(
                consumerTag = "testConsumer",
                deliveryTag = 1u,
                redelivered = false,
                exchange = "testExchange",
                routingKey = "testRoutingKey",
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicGet() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Get(
                reserved1 = 0u,
                queue = "testQueue",
                noAck = true
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicGetOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.GetOk(
                deliveryTag = 1u,
                redelivered = false,
                exchange = "testExchange",
                routingKey = "testRoutingKey",
                messageCount = 0u
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicGetEmpty() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.GetEmpty(
                reserved1 = ""
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicAck() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Ack(
                deliveryTag = 1u,
                multiple = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicReject() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Reject(
                deliveryTag = 1u,
                requeue = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicRecoverAsync() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.RecoverAsync(
                requeue = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicRecover() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Recover(
                requeue = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicRecoverOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.RecoverOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodBasicNack() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Basic.Nack(
                deliveryTag = 1u,
                multiple = false,
                requeue = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodConfirmSelect() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Confirm.Select(
                noWait = false
            )
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodConfirmSelectOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Confirm.SelectOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxSelect() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.Select
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxSelectOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.SelectOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxCommit() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.Commit
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxCommitOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.CommitOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxRollback() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.Rollback
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameMethodTxRollbackOk() {
        val frame = Frame(
            channelId = 1u,
            payload = Frame.Method.Tx.RollbackOk
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameHeartbeat() {
        val frame = Frame(
            channelId = 0u,
            payload = Frame.Heartbeat
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

    @Test
    fun testFrameBody() {
        val frame = Frame(
            channelId = 0u,
            payload = Frame.Body(byteArrayOf(1, 2, 3, 4, 5))
        )
        val encoded = ProtocolBinary.encodeToByteArray(frame)
        val decoded = ProtocolBinary.decodeFromByteArray<Frame>(encoded)
        assertEquals(frame, decoded)
    }

}
