package dev.kourier.amqp.serialization

import dev.kourier.amqp.ProtocolError
import kotlinx.io.Buffer
import kotlinx.io.writeDouble
import kotlinx.io.writeFloat
import kotlinx.io.writeString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

class ProtocolBinaryEncoder(
    val buffer: Buffer,
) : Encoder {

    override val serializersModule: SerializersModule = EmptySerializersModule()

    @ExperimentalSerializationApi
    override fun encodeNull() {
        // Nothing
    }

    override fun encodeBoolean(value: Boolean) {
        buffer.writeByte(if (value) 1.toByte() else 0.toByte())
    }

    override fun encodeByte(value: Byte) {
        buffer.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        buffer.writeShort(value)
    }

    override fun encodeChar(value: Char) {
        buffer.writeShort(value.code.toShort())
    }

    override fun encodeInt(value: Int) {
        buffer.writeInt(value)
    }

    override fun encodeLong(value: Long) {
        buffer.writeLong(value)
    }

    override fun encodeFloat(value: Float) {
        buffer.writeFloat(value)
    }

    override fun encodeDouble(value: Double) {
        return buffer.writeDouble(value)
    }

    override fun encodeString(value: String) {
        throw NotImplementedError("encodeString is not implemented. Use encodeShortString or encodeLongString instead.")
    }

    fun encodeShortString(shortString: String) {
        if (shortString.length > UByte.MAX_VALUE.toInt())
            throw ProtocolError.Invalid(shortString, "shortString too big, max: ${UByte.MAX_VALUE}", this)
        buffer.writeByte(shortString.length.toByte())
        buffer.writeString(shortString)
    }

    fun encodeLongString(longString: String) {
        if (longString.length.toUInt() > UInt.MAX_VALUE)
            throw ProtocolError.Invalid(longString, "longString too big, max: ${UInt.MAX_VALUE}", this)
        buffer.writeInt(longString.length)
        buffer.writeString(longString)
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        TODO("Not yet implemented")
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        TODO("Not yet implemented")
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        TODO("Not yet implemented")
    }

}
