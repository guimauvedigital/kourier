package dev.kourier.amqp

import dev.kourier.amqp.serialization.serializers.TableSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable(with = TableSerializer::class)
value class Table(
    val values: Map<String, Field>,
)
