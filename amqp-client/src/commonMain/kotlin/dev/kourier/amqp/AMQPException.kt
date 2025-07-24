package dev.kourier.amqp

import kotlinx.io.IOException

sealed class AMQPException : IOException() {

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
        val isInitiatedByApplication: Boolean = false,
    ) : AMQPException()

    data object TooManyOpenedChannels : AMQPException()

    data object ChannelNotInConfirmMode : AMQPException()

    data object ConsumerCancelled : AMQPException()

    data object ConsumerAlreadyCancelled : AMQPException()

    data object InvalidMessage : AMQPException()

    data class InvalidResponse(val response: AMQPResponse) : AMQPException()

}
