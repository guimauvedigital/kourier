package dev.kourier.amqp.robust

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal class ChannelRestoreContextElement : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*>
        get() = ChannelRestoreContextElement

    companion object : CoroutineContext.Key<ChannelRestoreContextElement>
}

internal suspend fun withChannelRestoreContext(block: suspend () -> Unit): Unit =
    withContext(ChannelRestoreContextElement()) { block() }

internal suspend fun currentChannelRestoreContext(): ChannelRestoreContextElement? =
    currentCoroutineContext()[ChannelRestoreContextElement]
