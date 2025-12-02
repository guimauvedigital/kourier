package dev.kourier.amqp.states

/**
 * Sets a prefetch limit when consuming messages.
 * No more messages will be delivered to the consumer until one or more messages have been acknowledged or rejected.
 */
class DeclaredQosBuilder {

    /**
     * Size of the limit.
     */
    var count: UShort = 0u

    /**
     * Whether the limit will be shared across all consumers on the channel.
     */
    var global: Boolean = false

    /**
     * Builds the [DeclaredQos] instance.
     *
     * @return The constructed [DeclaredQos].
     */
    fun build(): DeclaredQos {
        return DeclaredQos(
            count = count,
            global = global,
        )
    }

}
