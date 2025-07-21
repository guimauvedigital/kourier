package dev.kourier.amqp.connection

import kotlinx.coroutines.CoroutineScope

/**
 * Creates an [AMQPConnectionConfiguration] using a DSL builder.
 */
fun amqpConnectionConfiguration(block: AMQPConnectionConfigurationBuilder.() -> Unit): AMQPConnectionConfiguration {
    return AMQPConnectionConfigurationBuilder().apply(block).build()
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
    config: AMQPConnectionConfiguration,
): AMQPConnection = DefaultAMQPConnection.create(
    coroutineScope = coroutineScope,
    config = config,
)
