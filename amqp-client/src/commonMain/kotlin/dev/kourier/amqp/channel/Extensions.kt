package dev.kourier.amqp.channel

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.states.*

/**
 * Publish a ByteArray message using a [PublishedMessage] instance.
 *
 * @param publishedMessage The [PublishedMessage] instance containing the message properties.
 *
 * @return The published message response.
 */
suspend fun AMQPChannel.basicPublish(publishedMessage: PublishedMessage): AMQPResponse.Channel.Basic.Published {
    return this.basicPublish(
        body = publishedMessage.body,
        exchange = publishedMessage.exchange,
        routingKey = publishedMessage.routingKey,
        mandatory = publishedMessage.mandatory,
        immediate = publishedMessage.immediate,
        properties = publishedMessage.properties
    )
}

/**
 * Publish a ByteArray message using a [PublishedMessageBuilder] block.
 *
 * @param block The block to configure the [PublishedMessageBuilder].
 *
 * @return The published message response.
 */
suspend fun AMQPChannel.basicPublish(block: PublishedMessageBuilder.() -> Unit): AMQPResponse.Channel.Basic.Published {
    return this.basicPublish(publishedMessage(block))
}

/**
 * Get a single message using a [FetchedMessage] instance.
 *
 * @param fetchedMessage The [FetchedMessage] instance containing the fetch properties.
 *
 * @return The fetched message response.
 */
suspend fun AMQPChannel.basicGet(fetchedMessage: FetchedMessage): AMQPResponse.Channel.Message.Get {
    return this.basicGet(
        queue = fetchedMessage.queue,
        noAck = fetchedMessage.noAck
    )
}

/**
 * Get a single message using a [FetchedMessageBuilder] block.
 *
 * @param block The block to configure the [FetchedMessageBuilder].
 *
 * @return The fetched message response.
 */
suspend fun AMQPChannel.basicGet(block: FetchedMessageBuilder.() -> Unit): AMQPResponse.Channel.Message.Get {
    return this.basicGet(fetchedMessage(block))
}

/**
 * Recover unacknowledged messages using a [RecoveredMessages] instance.
 *
 * @param recoveredMessages The [RecoveredMessages] instance containing the recovery properties.
 *
 * @return The recovery response.
 */
suspend fun AMQPChannel.basicRecover(recoveredMessages: RecoveredMessages): AMQPResponse.Channel.Basic.Recovered {
    return this.basicRecover(
        requeue = recoveredMessages.requeue
    )
}

/**
 * Recover unacknowledged messages using a [RecoveredMessagesBuilder] block.
 *
 * @param block The block to configure the [RecoveredMessagesBuilder].
 *
 * @return The recovery response.
 */
suspend fun AMQPChannel.basicRecover(block: RecoveredMessagesBuilder.() -> Unit): AMQPResponse.Channel.Basic.Recovered {
    return this.basicRecover(recoveredMessages(block))
}

/**
 * Binds an exchange using a [BoundExchange] instance.
 *
 * @param boundExchange The [BoundExchange] instance containing the exchange binding properties.
 *
 * @return The bound exchange response.
 */
suspend fun AMQPChannel.exchangeBind(boundExchange: BoundExchange): AMQPResponse.Channel.Exchange.Bound {
    return this.exchangeBind(
        destination = boundExchange.destination,
        source = boundExchange.source,
        routingKey = boundExchange.routingKey,
        arguments = boundExchange.arguments
    )
}

/**
 * Binds an exchange using a [BoundExchangeBuilder] block.
 *
 * @param block The block to configure the [BoundExchangeBuilder].
 *
 * @return The bound exchange response.
 */
suspend fun AMQPChannel.exchangeBind(block: BoundExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Bound {
    return this.exchangeBind(boundExchange(block))
}

/**
 * Unbinds an exchange using a [UnboundExchange] instance.
 *
 * @param unboundExchange The [UnboundExchange] instance containing the exchange unbinding properties.
 *
 * @return The unbound exchange response.
 */
suspend fun AMQPChannel.exchangeUnbind(unboundExchange: UnboundExchange): AMQPResponse.Channel.Exchange.Unbound {
    return this.exchangeUnbind(
        destination = unboundExchange.destination,
        source = unboundExchange.source,
        routingKey = unboundExchange.routingKey,
        arguments = unboundExchange.arguments
    )
}

/**
 * Unbinds an exchange using a [UnboundExchangeBuilder] block.
 *
 * @param block The block to configure the [UnboundExchangeBuilder].
 *
 * @return The unbound exchange response.
 */
suspend fun AMQPChannel.exchangeUnbind(block: UnboundExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Unbound {
    return this.exchangeUnbind(unboundExchange(block))
}

/**
 * Binds a queue using a [BoundQueue] instance.
 *
 * @param boundQueue The [BoundQueue] instance containing the queue binding properties.
 *
 * @return The bound queue response.
 */
suspend fun AMQPChannel.queueBind(boundQueue: BoundQueue): AMQPResponse.Channel.Queue.Bound {
    return this.queueBind(
        queue = boundQueue.queue,
        exchange = boundQueue.exchange,
        routingKey = boundQueue.routingKey,
        arguments = boundQueue.arguments
    )
}

/**
 * Binds a queue using a [BoundQueueBuilder] block.
 *
 * @param block The block to configure the [BoundQueueBuilder].
 *
 * @return The bound queue response.
 */
suspend fun AMQPChannel.queueBind(block: BoundQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Bound {
    return this.queueBind(boundQueue(block))
}

/**
 * Unbinds a queue using a [UnboundQueue] instance.
 *
 * @param unboundQueue The [UnboundQueue] instance containing the queue unbinding properties.
 *
 * @return The unbound queue response.
 */
suspend fun AMQPChannel.queueUnbind(unboundQueue: UnboundQueue): AMQPResponse.Channel.Queue.Unbound {
    return this.queueUnbind(
        queue = unboundQueue.queue,
        exchange = unboundQueue.exchange,
        routingKey = unboundQueue.routingKey,
        arguments = unboundQueue.arguments
    )
}

/**
 * Unbinds a queue using a [UnboundQueueBuilder] block.
 *
 * @param block The block to configure the [UnboundQueueBuilder].
 *
 * @return The unbound queue response.
 */
suspend fun AMQPChannel.queueUnbind(block: UnboundQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Unbound {
    return this.queueUnbind(unboundQueue(block))
}

/**
 * Declares an exchange using a [DeclaredExchange] instance.
 *
 * @param declaredExchange The [DeclaredExchange] instance containing the exchange properties.
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclare(declaredExchange: DeclaredExchange): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclare(
        name = declaredExchange.name,
        type = declaredExchange.type,
        durable = declaredExchange.durable,
        autoDelete = declaredExchange.autoDelete,
        internal = declaredExchange.internal,
        arguments = declaredExchange.arguments
    )
}

/**
 * Declares an exchange using a [DeclaredExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredExchangeBuilder].
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclare(block: DeclaredExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclare(declaredExchange(block))
}

/**
 * Passively declares an exchange using a [DeclaredPassiveExchange] instance.
 *
 * @param declaredPassiveExchange The [DeclaredPassiveExchange] instance containing the exchange name.
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclarePassive(declaredPassiveExchange: DeclaredPassiveExchange): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclarePassive(
        name = declaredPassiveExchange.name
    )
}

/**
 * Passively declares an exchange using a [DeclaredPassiveExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredPassiveExchangeBuilder].
 *
 * @return The declared exchange response.
 */
suspend fun AMQPChannel.exchangeDeclarePassive(block: DeclaredPassiveExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Declared {
    return this.exchangeDeclarePassive(declaredPassiveExchange(block))
}

/**
 * Deletes an exchange using a [DeletedExchange] instance.
 *
 * @param deletedExchange The [DeletedExchange] instance containing the deletion properties.
 *
 * @return The deleted exchange response.
 */
suspend fun AMQPChannel.exchangeDelete(deletedExchange: DeletedExchange): AMQPResponse.Channel.Exchange.Deleted {
    return this.exchangeDelete(
        name = deletedExchange.name,
        ifUnused = deletedExchange.ifUnused
    )
}

/**
 * Deletes an exchange using a [DeletedExchangeBuilder] block.
 *
 * @param block The block to configure the [DeletedExchangeBuilder].
 *
 * @return The deleted exchange response.
 */
suspend fun AMQPChannel.exchangeDelete(block: DeletedExchangeBuilder.() -> Unit): AMQPResponse.Channel.Exchange.Deleted {
    return this.exchangeDelete(deletedExchange(block))
}

/**
 * Declares QoS settings using a [DeclaredQos] instance.
 *
 * @param declaredQos The [DeclaredQos] instance containing the QoS properties.
 *
 * @return The QoS declaration response.
 */
suspend fun AMQPChannel.basicQos(declaredQos: DeclaredQos): AMQPResponse.Channel.Basic.QosOk {
    return this.basicQos(
        count = declaredQos.count,
        global = declaredQos.global
    )
}

/**
 * Declares QoS settings using a [DeclaredQosBuilder] block.
 *
 * @param block The block to configure the [DeclaredQosBuilder].
 *
 * @return The QoS declaration response.
 */
suspend fun AMQPChannel.basicQos(block: DeclaredQosBuilder.() -> Unit): AMQPResponse.Channel.Basic.QosOk {
    return this.basicQos(declaredQos(block))
}

/**
 * Declares a queue using a [DeclaredQueue] instance.
 *
 * @param declaredQueue The [DeclaredQueue] instance containing the queue properties.
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclare(declaredQueue: DeclaredQueue): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclare(
        name = declaredQueue.name,
        durable = declaredQueue.durable,
        exclusive = declaredQueue.exclusive,
        autoDelete = declaredQueue.autoDelete,
        arguments = declaredQueue.arguments
    )
}

/**
 * Declares a queue using a [DeclaredQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredQueueBuilder].
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclare(block: DeclaredQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclare(declaredQueue(block))
}

/**
 * Passively declares a queue using a [DeclaredPassiveQueue] instance.
 *
 * @param declaredPassiveQueue The [DeclaredPassiveQueue] instance containing the queue name.
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclarePassive(declaredPassiveQueue: DeclaredPassiveQueue): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclarePassive(
        name = declaredPassiveQueue.name
    )
}

/**
 * Passively declares a queue using a [DeclaredPassiveQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredPassiveQueueBuilder].
 *
 * @return The declared queue response.
 */
suspend fun AMQPChannel.queueDeclarePassive(block: DeclaredPassiveQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Declared {
    return this.queueDeclarePassive(declaredPassiveQueue(block))
}

/**
 * Deletes a queue using a [DeletedQueue] instance.
 *
 * @param deletedQueue The [DeletedQueue] instance containing the deletion properties.
 *
 * @return The deleted queue response.
 */
suspend fun AMQPChannel.queueDelete(deletedQueue: DeletedQueue): AMQPResponse.Channel.Queue.Deleted {
    return this.queueDelete(
        name = deletedQueue.name,
        ifUnused = deletedQueue.ifUnused,
        ifEmpty = deletedQueue.ifEmpty
    )
}

/**
 * Deletes a queue using a [DeletedQueueBuilder] block.
 *
 * @param block The block to configure the [DeletedQueueBuilder].
 *
 * @return The deleted queue response.
 */
suspend fun AMQPChannel.queueDelete(block: DeletedQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Deleted {
    return this.queueDelete(deletedQueue(block))
}

/**
 * Purges a queue using a [PurgedQueue] instance.
 *
 * @param purgedQueue The [PurgedQueue] instance containing the queue name.
 *
 * @return The purged queue response.
 */
suspend fun AMQPChannel.queuePurge(purgedQueue: PurgedQueue): AMQPResponse.Channel.Queue.Purged {
    return this.queuePurge(
        name = purgedQueue.name
    )
}

/**
 * Purges a queue using a [PurgedQueueBuilder] block.
 *
 * @param block The block to configure the [PurgedQueueBuilder].
 *
 * @return The purged queue response.
 */
suspend fun AMQPChannel.queuePurge(block: PurgedQueueBuilder.() -> Unit): AMQPResponse.Channel.Queue.Purged {
    return this.queuePurge(purgedQueue(block))
}

/**
 * Set channel in publish confirm mode using a [SelectedConfirmMode] instance.
 *
 * @param selectedConfirmMode The [SelectedConfirmMode] instance.
 *
 * @return The confirm selection response.
 */
suspend fun AMQPChannel.confirmSelect(selectedConfirmMode: SelectedConfirmMode): AMQPResponse.Channel.Confirm.Selected {
    return this.confirmSelect()
}

/**
 * Set channel in publish confirm mode using a [SelectedConfirmModeBuilder] block.
 *
 * @param block The block to configure the [SelectedConfirmModeBuilder].
 *
 * @return The confirm selection response.
 */
suspend fun AMQPChannel.confirmSelect(block: SelectedConfirmModeBuilder.() -> Unit = {}): AMQPResponse.Channel.Confirm.Selected {
    return this.confirmSelect(selectedConfirmMode(block))
}

/**
 * Set channel in transaction mode using a [SelectedTransactionMode] instance.
 *
 * @param selectedTransactionMode The [SelectedTransactionMode] instance.
 *
 * @return The transaction selection response.
 */
suspend fun AMQPChannel.txSelect(selectedTransactionMode: SelectedTransactionMode): AMQPResponse.Channel.Tx.Selected {
    return this.txSelect()
}

/**
 * Set channel in transaction mode using a [SelectedTransactionModeBuilder] block.
 *
 * @param block The block to configure the [SelectedTransactionModeBuilder].
 *
 * @return The transaction selection response.
 */
suspend fun AMQPChannel.txSelect(block: SelectedTransactionModeBuilder.() -> Unit = {}): AMQPResponse.Channel.Tx.Selected {
    return this.txSelect(selectedTransactionMode(block))
}

/**
 * Commit a transaction using a [CommittedTransaction] instance.
 *
 * @param committedTransaction The [CommittedTransaction] instance.
 *
 * @return The transaction commit response.
 */
suspend fun AMQPChannel.txCommit(committedTransaction: CommittedTransaction): AMQPResponse.Channel.Tx.Committed {
    return this.txCommit()
}

/**
 * Commit a transaction using a [CommittedTransactionBuilder] block.
 *
 * @param block The block to configure the [CommittedTransactionBuilder].
 *
 * @return The transaction commit response.
 */
suspend fun AMQPChannel.txCommit(block: CommittedTransactionBuilder.() -> Unit = {}): AMQPResponse.Channel.Tx.Committed {
    return this.txCommit(committedTransaction(block))
}

/**
 * Rollback a transaction using a [RolledbackTransaction] instance.
 *
 * @param rolledbackTransaction The [RolledbackTransaction] instance.
 *
 * @return The transaction rollback response.
 */
suspend fun AMQPChannel.txRollback(rolledbackTransaction: RolledbackTransaction): AMQPResponse.Channel.Tx.Rollbacked {
    return this.txRollback()
}

/**
 * Rollback a transaction using a [RolledbackTransactionBuilder] block.
 *
 * @param block The block to configure the [RolledbackTransactionBuilder].
 *
 * @return The transaction rollback response.
 */
suspend fun AMQPChannel.txRollback(block: RolledbackTransactionBuilder.() -> Unit = {}): AMQPResponse.Channel.Tx.Rollbacked {
    return this.txRollback(rolledbackTransaction(block))
}

/**
 * Send a flow message to broker using a [ControlledFlow] instance.
 *
 * @param controlledFlow The [ControlledFlow] instance containing the flow properties.
 *
 * @return The flow response.
 */
suspend fun AMQPChannel.flow(controlledFlow: ControlledFlow): AMQPResponse.Channel.Flowed {
    return this.flow(
        active = controlledFlow.active
    )
}

/**
 * Send a flow message to broker using a [ControlledFlowBuilder] block.
 *
 * @param block The block to configure the [ControlledFlowBuilder].
 *
 * @return The flow response.
 */
suspend fun AMQPChannel.flow(block: ControlledFlowBuilder.() -> Unit): AMQPResponse.Channel.Flowed {
    return this.flow(controlledFlow(block))
}
