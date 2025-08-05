package dev.kourier.amqp.connection

import dev.kourier.amqp.Frame
import dev.kourier.amqp.serialization.ProtocolBinary
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToByteArray

class FakeServer(coroutineScope: CoroutineScope) {

    val serverReady = CompletableDeferred<Unit>()
    val serverClosed = CompletableDeferred<Unit>()

    val serverJob = coroutineScope.launch {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val server = aSocket(selectorManager).tcp().bind("127.0.0.1", 5673)
        serverReady.complete(Unit)
        val clientSocket = server.accept()

        val readChannel = clientSocket.openReadChannel()
        val writeChannel = clientSocket.openWriteChannel(autoFlush = true)

        readChannel.readAvailable(ByteArray(1024))
        writeChannel.writeByteArray(
            ProtocolBinary.encodeToByteArray(
                Frame(
                    channelId = 0u,
                    payload = Frame.Method.Connection.Tune(
                        channelMax = 1u,
                        frameMax = 4096u,
                        heartbeat = 60u,
                    )
                )
            )
        )
        readChannel.readAvailable(ByteArray(1024))
        writeChannel.writeByteArray(
            ProtocolBinary.encodeToByteArray(
                Frame(
                    channelId = 0u,
                    payload = Frame.Method.Connection.OpenOk(
                        reserved1 = ""
                    )
                )
            )
        )

        delay(100)
        clientSocket.close() // Simulate server closing connection abruptly
        server.close()
        delay(100)
        serverClosed.complete(Unit)
    }

}
