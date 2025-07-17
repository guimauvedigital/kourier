package dev.kourier.amqp

sealed class ProtocolError : Exception() {

    data class Decode(
        val type: Any? = null,
        val kind: Field.Kind? = null,
        override val message: String? = null,
        val context: Any,
        val inner: Throwable? = null,
    ) : ProtocolError()

    data class Encode(
        val value: Any? = null,
        val type: Any? = null,
        override val message: String? = null,
        val context: Any,
        val inner: Throwable? = null,
    ) : ProtocolError()

    data class Invalid(
        val value: Any,
        override val message: String? = null,
        val context: Any,
    ) : ProtocolError()

    data class Incomplete(
        val type: Any? = null,
        val expected: UInt,
        val got: Int,
    ) : ProtocolError()

}
