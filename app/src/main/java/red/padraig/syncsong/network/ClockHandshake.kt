package red.padraig.syncsong.network

import android.util.Log
import org.java_websocket.client.WebSocketClient
import red.padraig.syncsong.ordinal
import red.padraig.syncsong.tag

class ClockHandshake(private val socket: WebSocketClient?, private val handshakeComplete: () -> Unit) {

    var messageCounter = 1

    // Responds to a handshake message with the current timestamp.
    // Calls the handshakeComplete callback when it receives a timestamp of 0.
    fun parseMessage(msg: Message) {
        val serverTime = msg.timestamp
        if (serverTime == 0L) {
            Log.d(this.tag(), "Received empty timestamp, handshake complete")
            handshakeComplete()
            return
        }
        Log.d(this.tag(), "Received ${messageCounter.ordinal()} handshake message with time $serverTime")
        val appTime = System.currentTimeMillis()
        Log.d(this.tag(), "Sending handshake message with time $appTime (difference: ${appTime-serverTime})")
        socket?.send(Message.marshal(Message(
                command = ClientCommand.Handshake.ordinal,
                timestamp = appTime
        )))
        messageCounter++
    }
}