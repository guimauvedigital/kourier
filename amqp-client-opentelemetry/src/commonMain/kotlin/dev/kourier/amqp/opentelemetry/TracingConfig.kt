package dev.kourier.amqp.opentelemetry

/**
 * Configuration options for OpenTelemetry tracing behavior.
 *
 * @property traceConnectionOperations Whether to create spans for connection operations (open, close, heartbeat).
 *           Default: false (to reduce noise).
 * @property traceChannelManagementOperations Whether to create spans for channel management operations
 *           (queueDeclare, exchangeDeclare, bindings, QoS, etc.). Default: false (to reduce noise).
 * @property captureMessageBody Whether to capture message bodies as span attributes.
 *           Warning: May expose sensitive data. Default: false.
 * @property maxBodySizeToCapture Maximum size of message body to capture (if enabled). Default: 1024 bytes.
 * @property publishSpanNameFormatter Custom span name formatter for publish operations.
 *           Default: "{exchange} send" or "{routingKey} send" if exchange is empty.
 * @property consumeSpanNameFormatter Custom span name formatter for consume operations.
 *           Default: "{queue} receive".
 */
data class TracingConfig(
    val traceConnectionOperations: Boolean = false,
    val traceChannelManagementOperations: Boolean = false,
    val captureMessageBody: Boolean = false,
    val maxBodySizeToCapture: Int = 1024,
    val publishSpanNameFormatter: (exchange: String, routingKey: String) -> String = defaultPublishSpanNameFormatter,
    val consumeSpanNameFormatter: (queue: String) -> String = defaultConsumeSpanNameFormatter,
) {

    companion object {

        /**
         * Default configuration with minimal overhead.
         */
        fun default() = TracingConfig()

        /**
         * Configuration for debugging with more detailed tracing.
         */
        fun debug() = TracingConfig(
            traceConnectionOperations = true,
            traceChannelManagementOperations = true,
            captureMessageBody = true,
        )

        /**
         * Default publish span name formatter.
         * Returns "{exchange} send" or "{routingKey} send" if exchange is empty.
         */
        val defaultPublishSpanNameFormatter: (String, String) -> String = { exchange, routingKey ->
            if (exchange.isNotEmpty()) "$exchange send" else "$routingKey send"
        }

        /**
         * Default consume span name formatter.
         * Returns "{queue} receive".
         */
        val defaultConsumeSpanNameFormatter: (String) -> String = { queue ->
            "$queue receive"
        }

    }

}
