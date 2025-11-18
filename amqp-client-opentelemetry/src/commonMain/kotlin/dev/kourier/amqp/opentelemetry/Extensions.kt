package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import io.opentelemetry.api.trace.Tracer

/**
 * Wraps an existing connection with OpenTelemetry instrumentation.
 *
 * This extension function allows you to add tracing to an existing AMQP connection.
 * All channels opened through the wrapped connection will automatically be traced.
 *
 * @param tracer The OpenTelemetry tracer to use for creating spans.
 * @param tracingConfig Configuration for tracing behavior. Defaults to [TracingConfig.default].
 * @return An instrumented AMQPConnection that wraps this connection.
 *
 * @sample
 * ```kotlin
 * val connection = createAMQPConnection(this, config)
 * val tracedConnection = connection.withTracing(tracer)
 * ```
 */
fun AMQPConnection.withTracing(
    tracer: Tracer,
    tracingConfig: TracingConfig = TracingConfig.default(),
): AMQPConnection {
    return OpenTelemetryAMQPConnection(this, tracer, tracingConfig)
}

/**
 * Wraps an existing channel with OpenTelemetry instrumentation.
 *
 * This extension function allows you to add tracing to an existing AMQP channel.
 * All publish and consume operations will automatically be traced.
 *
 * @param tracer The OpenTelemetry tracer to use for creating spans.
 * @param tracingConfig Configuration for tracing behavior. Defaults to [TracingConfig.default].
 * @return An instrumented AMQPChannel that wraps this channel.
 *
 * @sample
 * ```kotlin
 * val channel = connection.openChannel()
 * val tracedChannel = channel.withTracing(tracer)
 * ```
 */
fun AMQPChannel.withTracing(
    tracer: Tracer,
    tracingConfig: TracingConfig = TracingConfig.default(),
): AMQPChannel {
    return OpenTelemetryAMQPChannel(this, tracer, tracingConfig)
}
