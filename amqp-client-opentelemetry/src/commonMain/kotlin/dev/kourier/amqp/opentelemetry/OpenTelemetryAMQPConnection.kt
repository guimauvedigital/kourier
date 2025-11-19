package dev.kourier.amqp.opentelemetry

import dev.kourier.amqp.AMQPException
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Frame
import dev.kourier.amqp.InternalAmqpApi
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConfig
import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.connection.ConnectionState
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.Flow

/**
 * OpenTelemetry-instrumented wrapper around [AMQPConnection].
 *
 * This class wraps an existing AMQP connection and provides automatic tracing
 * for connection operations. All channels opened through this connection will
 * automatically be wrapped with [OpenTelemetryAMQPChannel].
 *
 * @property delegate The underlying AMQP connection to wrap.
 * @property tracer The OpenTelemetry tracer to use for creating spans.
 * @property tracingConfig Configuration for tracing behavior.
 */
class OpenTelemetryAMQPConnection(
    private val delegate: AMQPConnection,
    private val tracer: Tracer,
    private val tracingConfig: TracingConfig = TracingConfig.default(),
) : AMQPConnection by delegate {

    override suspend fun openChannel(): AMQPChannel {
        val channel = if (tracingConfig.traceConnectionOperations) {
            executeInSpan("connection.openChannel", SpanKind.CLIENT) {
                delegate.openChannel()
            }
        } else {
            delegate.openChannel()
        }

        // Wrap the channel with OpenTelemetry instrumentation
        return OpenTelemetryAMQPChannel(channel, tracer, tracingConfig)
    }

    override suspend fun sendHeartbeat() {
        if (tracingConfig.traceConnectionOperations) {
            executeInSpan("connection.heartbeat", SpanKind.CLIENT) {
                delegate.sendHeartbeat()
            }
        } else {
            delegate.sendHeartbeat()
        }
    }

    override suspend fun close(reason: String, code: UShort): AMQPResponse.Connection.Closed {
        return if (tracingConfig.traceConnectionOperations) {
            executeInSpan("connection.close", SpanKind.CLIENT) { span ->
                span.setAttribute("messaging.operation", "close")
                if (reason.isNotEmpty()) {
                    span.setAttribute("messaging.close_reason", reason)
                }
                span.setAttribute("messaging.close_code", code.toLong())
                delegate.close(reason, code)
            }
        } else {
            delegate.close(reason, code)
        }
    }

    /**
     * Helper function to execute code within a span.
     */
    private suspend fun <T> executeInSpan(
        spanName: String,
        spanKind: SpanKind,
        block: suspend (Span) -> T,
    ): T {
        val span = tracer.spanBuilder(spanName)
            .setSpanKind(spanKind)
            .startSpan()

        return try {
            span.makeCurrent().use {
                val result = block(span)
                span.setStatus(StatusCode.OK)
                result
            }
        } catch (e: Exception) {
            span.recordException(e)
            span.setStatus(StatusCode.ERROR, e.message ?: "Error in operation")
            throw e
        } finally {
            span.end()
        }
    }

}
