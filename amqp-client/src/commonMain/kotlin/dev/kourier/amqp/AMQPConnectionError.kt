package dev.kourier.amqp

sealed class AMQPConnectionError : Exception() {

    data object InvalidUrl : AMQPConnectionError()
    data object InvalidUrlScheme : AMQPConnectionError()
    data class ConnectionClosed(val replyCode: UShort? = null, val replyText: String? = null) : AMQPConnectionError()
    data class ConnectionClose(val broker: Throwable? = null, val connection: Throwable? = null) : AMQPConnectionError()
    data object ConnectionBlocked : AMQPConnectionError()
    data class ChannelClosed(val replyCode: UShort? = null, val replyText: String? = null) : AMQPConnectionError()
    data object TooManyOpenedChannels : AMQPConnectionError()
    data object ChannelNotInConfirmMode : AMQPConnectionError()
    data object ConsumerCancelled : AMQPConnectionError()
    data object ConsumerAlreadyCancelled : AMQPConnectionError()
    data object InvalidMessage : AMQPConnectionError()
    data class InvalidResponse(val response: AMQPResponse) : AMQPConnectionError()

}
