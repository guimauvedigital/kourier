package dev.kourier.amqp.serialization

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

object ProtocolBinary : BinaryFormat {

    override val serializersModule: SerializersModule = EmptySerializersModule()

    override fun <T> encodeToByteArray(
        serializer: SerializationStrategy<T>,
        value: T,
    ): ByteArray {
        ProtocolBinaryEncoder(Buffer()).apply {
            encodeSerializableValue(serializer, value)
        }.let { encoder ->
            return encoder.buffer.readByteArray()
        }
    }

    override fun <T> decodeFromByteArray(
        deserializer: DeserializationStrategy<T>,
        bytes: ByteArray,
    ): T {
        ProtocolBinaryDecoder(Buffer()).apply {
            buffer.write(bytes)
            return decodeSerializableValue(deserializer)
        }
    }

}
