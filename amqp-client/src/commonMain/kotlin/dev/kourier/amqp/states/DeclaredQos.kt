package dev.kourier.amqp.states

/**
 * Sets a prefetch limit when consuming messages.
 * No more messages will be delivered to the consumer until one or more messages have been acknowledged or rejected.
 */
data class DeclaredQos(
    /**
     * Size of the limit.
     */
    val count: UShort,
    /**
     * Whether the limit will be shared across all consumers on the channel.
     */
    val global: Boolean,
)
