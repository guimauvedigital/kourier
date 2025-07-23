package dev.kourier.amqp

sealed class AMQPException : Exception() {

    data object InvalidUrl : AMQPException()

    data object InvalidUrlScheme : AMQPException()

    data class ConnectionClosed(
        val replyCode: UShort? = null,
        val replyText: String? = null,
        val isInitiatedByApplication: Boolean = false,
    ) : AMQPException()

    data class ConnectionClose(val broker: Throwable? = null, val connection: Throwable? = null) : AMQPException()

    data object ConnectionBlocked : AMQPException()

    data class ChannelClosed(
        val replyCode: UShort? = null,
        val replyText: String? = null,
    ) : AMQPException()

    data object TooManyOpenedChannels : AMQPException()

    data object ChannelNotInConfirmMode : AMQPException()

    data object ConsumerCancelled : AMQPException()

    data object ConsumerAlreadyCancelled : AMQPException()

    data object InvalidMessage : AMQPException()

    data class InvalidResponse(val response: AMQPResponse) : AMQPException()

}
