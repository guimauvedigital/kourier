package dev.kourier.amqp.connection

import kotlinx.coroutines.CoroutineScope

/**
 * Creates an [AMQPConfig] using a DSL builder.
 */
fun amqpConfig(block: AMQPConfigBuilder.() -> Unit): AMQPConfig {
    return AMQPConfigBuilder().apply(block).build()
}

/**
 * Connect to broker.
 *
 * @param coroutineScope CoroutineScope on which to connect.
 * @param config Configuration data.
 *
 * @return AMQPConnection instance.
 */
suspend fun createAMQPConnection(
    coroutineScope: CoroutineScope,
    config: AMQPConfig,
): AMQPConnection = DefaultAMQPConnection.create(
    coroutineScope = coroutineScope,
    config = config,
)

/**
 * Connect to broker.
 *
 * @param coroutineScope CoroutineScope on which to connect.
 * @param block The configuration block to apply to the AMQPConfigBuilder.
 *
 * @return AMQPConnection instance.
 */
suspend fun createAMQPConnection(
    coroutineScope: CoroutineScope,
    block: AMQPConfigBuilder.() -> Unit,
): AMQPConnection = createAMQPConnection(coroutineScope, amqpConfig(block))
