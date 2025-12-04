package dev.kourier.amqp.states

/**
 * Binds an exchange using a [BoundExchangeBuilder] block.
 *
 * @param block The block to configure the [BoundExchangeBuilder].
 *
 * @return The constructed [BoundExchange] instance.
 */
fun boundExchange(block: BoundExchangeBuilder.() -> Unit): BoundExchange {
    return BoundExchangeBuilder().apply(block).build()
}

/**
 * Commits a transaction using a [CommittedTransactionBuilder] block.
 *
 * @param block The block to configure the [CommittedTransactionBuilder].
 *
 * @return The constructed [CommittedTransaction] instance.
 */
fun committedTransaction(block: CommittedTransactionBuilder.() -> Unit = {}): CommittedTransaction {
    return CommittedTransactionBuilder().apply(block).build()
}

/**
 * Controls flow using a [ControlledFlowBuilder] block.
 *
 * @param block The block to configure the [ControlledFlowBuilder].
 *
 * @return The constructed [ControlledFlow] instance.
 */
fun controlledFlow(block: ControlledFlowBuilder.() -> Unit): ControlledFlow {
    return ControlledFlowBuilder().apply(block).build()
}

/**
 * Binds a queue using a [BoundQueueBuilder] block.
 *
 * @param block The block to configure the [BoundQueueBuilder].
 *
 * @return The constructed [BoundQueue] instance.
 */
fun boundQueue(block: BoundQueueBuilder.() -> Unit): BoundQueue {
    return BoundQueueBuilder().apply(block).build()
}

/**
 * Declares an exchange using a [DeclaredExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredExchangeBuilder].
 *
 * @return The constructed [DeclaredExchange] instance.
 */
fun declaredExchange(block: DeclaredExchangeBuilder.() -> Unit): DeclaredExchange {
    return DeclaredExchangeBuilder().apply(block).build()
}

/**
 * Passively declares an exchange using a [DeclaredPassiveExchangeBuilder] block.
 *
 * @param block The block to configure the [DeclaredPassiveExchangeBuilder].
 *
 * @return The constructed [DeclaredPassiveExchange] instance.
 */
fun declaredPassiveExchange(block: DeclaredPassiveExchangeBuilder.() -> Unit): DeclaredPassiveExchange {
    return DeclaredPassiveExchangeBuilder().apply(block).build()
}

/**
 * Passively declares a queue using a [DeclaredPassiveQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredPassiveQueueBuilder].
 *
 * @return The constructed [DeclaredPassiveQueue] instance.
 */
fun declaredPassiveQueue(block: DeclaredPassiveQueueBuilder.() -> Unit): DeclaredPassiveQueue {
    return DeclaredPassiveQueueBuilder().apply(block).build()
}

/**
 * Declares QoS settings using a [DeclaredQosBuilder] block.
 *
 * @param block The block to configure the [DeclaredQosBuilder].
 *
 * @return The constructed [DeclaredQos] instance.
 */
fun declaredQos(block: DeclaredQosBuilder.() -> Unit): DeclaredQos {
    return DeclaredQosBuilder().apply(block).build()
}

/**
 * Declares a queue using a [DeclaredQueueBuilder] block.
 *
 * @param block The block to configure the [DeclaredQueueBuilder].
 *
 * @return The constructed [DeclaredQueue] instance.
 */
fun declaredQueue(block: DeclaredQueueBuilder.() -> Unit): DeclaredQueue {
    return DeclaredQueueBuilder().apply(block).build()
}

/**
 * Deletes an exchange using a [DeletedExchangeBuilder] block.
 *
 * @param block The block to configure the [DeletedExchangeBuilder].
 *
 * @return The constructed [DeletedExchange] instance.
 */
fun deletedExchange(block: DeletedExchangeBuilder.() -> Unit): DeletedExchange {
    return DeletedExchangeBuilder().apply(block).build()
}

/**
 * Deletes a queue using a [DeletedQueueBuilder] block.
 *
 * @param block The block to configure the [DeletedQueueBuilder].
 *
 * @return The constructed [DeletedQueue] instance.
 */
fun deletedQueue(block: DeletedQueueBuilder.() -> Unit): DeletedQueue {
    return DeletedQueueBuilder().apply(block).build()
}

/**
 * Fetches a message using a [FetchedMessageBuilder] block.
 *
 * @param block The block to configure the [FetchedMessageBuilder].
 *
 * @return The constructed [FetchedMessage] instance.
 */
fun fetchedMessage(block: FetchedMessageBuilder.() -> Unit): FetchedMessage {
    return FetchedMessageBuilder().apply(block).build()
}

/**
 * Publishes a message using a [PublishedMessageBuilder] block.
 *
 * @param block The block to configure the [PublishedMessageBuilder].
 *
 * @return The constructed [PublishedMessage] instance.
 */
fun publishedMessage(block: PublishedMessageBuilder.() -> Unit): PublishedMessage {
    return PublishedMessageBuilder().apply(block).build()
}

/**
 * Purges a queue using a [PurgedQueueBuilder] block.
 *
 * @param block The block to configure the [PurgedQueueBuilder].
 *
 * @return The constructed [PurgedQueue] instance.
 */
fun purgedQueue(block: PurgedQueueBuilder.() -> Unit): PurgedQueue {
    return PurgedQueueBuilder().apply(block).build()
}

/**
 * Recovers messages using a [RecoveredMessagesBuilder] block.
 *
 * @param block The block to configure the [RecoveredMessagesBuilder].
 *
 * @return The constructed [RecoveredMessages] instance.
 */
fun recoveredMessages(block: RecoveredMessagesBuilder.() -> Unit): RecoveredMessages {
    return RecoveredMessagesBuilder().apply(block).build()
}

/**
 * Rolls back a transaction using a [RolledbackTransactionBuilder] block.
 *
 * @param block The block to configure the [RolledbackTransactionBuilder].
 *
 * @return The constructed [RolledbackTransaction] instance.
 */
fun rolledbackTransaction(block: RolledbackTransactionBuilder.() -> Unit = {}): RolledbackTransaction {
    return RolledbackTransactionBuilder().apply(block).build()
}

/**
 * Selects confirm mode using a [SelectedConfirmModeBuilder] block.
 *
 * @param block The block to configure the [SelectedConfirmModeBuilder].
 *
 * @return The constructed [SelectedConfirmMode] instance.
 */
fun selectedConfirmMode(block: SelectedConfirmModeBuilder.() -> Unit = {}): SelectedConfirmMode {
    return SelectedConfirmModeBuilder().apply(block).build()
}

/**
 * Selects transaction mode using a [SelectedTransactionModeBuilder] block.
 *
 * @param block The block to configure the [SelectedTransactionModeBuilder].
 *
 * @return The constructed [SelectedTransactionMode] instance.
 */
fun selectedTransactionMode(block: SelectedTransactionModeBuilder.() -> Unit = {}): SelectedTransactionMode {
    return SelectedTransactionModeBuilder().apply(block).build()
}

/**
 * Unbinds an exchange using a [UnboundExchangeBuilder] block.
 *
 * @param block The block to configure the [UnboundExchangeBuilder].
 *
 * @return The constructed [UnboundExchange] instance.
 */
fun unboundExchange(block: UnboundExchangeBuilder.() -> Unit): UnboundExchange {
    return UnboundExchangeBuilder().apply(block).build()
}

/**
 * Unbinds a queue using a [UnboundQueueBuilder] block.
 *
 * @param block The block to configure the [UnboundQueueBuilder].
 *
 * @return The constructed [UnboundQueue] instance.
 */
fun unboundQueue(block: UnboundQueueBuilder.() -> Unit): UnboundQueue {
    return UnboundQueueBuilder().apply(block).build()
}
