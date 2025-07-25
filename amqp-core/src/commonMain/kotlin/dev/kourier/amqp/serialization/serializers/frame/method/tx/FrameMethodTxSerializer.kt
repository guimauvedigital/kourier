package dev.kourier.amqp.serialization.serializers.frame.method.tx

import dev.kourier.amqp.Frame
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object FrameMethodTxSerializer : KSerializer<Frame.Method.Tx> {

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor
        get() = buildSerialDescriptor("Frame.Method.Tx", StructureKind.OBJECT)

    override fun serialize(encoder: Encoder, value: Frame.Method.Tx) {
        encoder.encodeShort(value.txKind.value.toShort())
        when (value) {
            is Frame.Method.Tx.Select -> {}
            is Frame.Method.Tx.SelectOk -> {}
            is Frame.Method.Tx.Commit -> {}
            is Frame.Method.Tx.CommitOk -> {}
            is Frame.Method.Tx.Rollback -> {}
            is Frame.Method.Tx.RollbackOk -> {}
        }
    }

    override fun deserialize(decoder: Decoder): Frame.Method.Tx {
        val kind = decoder.decodeShort().toUShort().let { byte ->
            Frame.Method.Tx.Kind.entries.first { it.value == byte }
        }
        return when (kind) {
            Frame.Method.Tx.Kind.SELECT -> Frame.Method.Tx.Select
            Frame.Method.Tx.Kind.SELECT_OK -> Frame.Method.Tx.SelectOk
            Frame.Method.Tx.Kind.COMMIT -> Frame.Method.Tx.Commit
            Frame.Method.Tx.Kind.COMMIT_OK -> Frame.Method.Tx.CommitOk
            Frame.Method.Tx.Kind.ROLLBACK -> Frame.Method.Tx.Rollback
            Frame.Method.Tx.Kind.ROLLBACK_OK -> Frame.Method.Tx.RollbackOk
        }
    }

}
