package dev.kourier.amqp

class AMQPChannels(
    private val channelMax: UShort,
) {

    private sealed class ChannelSlot {
        data object Reserved : ChannelSlot()
        data class Channel(val channel: AMQPChannel) : ChannelSlot()
    }

    private val channels: MutableMap<UShort, ChannelSlot> = mutableMapOf()

    fun get(id: UShort): AMQPChannel? {
        return when (val slot = channels[id]) {
            is ChannelSlot.Channel -> slot.channel
            else -> null
        }
    }

    fun reserveNext(): UShort? {
        if (channels.size >= channelMax.toInt()) return null
        for (i in 1u..channelMax.toUInt()) {
            val index = i.toUShort()
            if (channels[index] == null) {
                channels[index] = ChannelSlot.Reserved
                return index
            }
        }
        return null
    }

    fun add(channel: AMQPChannel) {
        channels[channel.id] = ChannelSlot.Channel(channel)
    }

    fun remove(id: UShort) {
        channels.remove(id)
    }

}
