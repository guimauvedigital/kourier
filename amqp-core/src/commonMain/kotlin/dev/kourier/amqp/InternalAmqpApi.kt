package dev.kourier.amqp

@RequiresOptIn(
    message = "This API is internal to the AMQP library. We recommend using the public APIs instead.",
    level = RequiresOptIn.Level.ERROR
)
@Retention(AnnotationRetention.BINARY)
annotation class InternalAmqpApi
