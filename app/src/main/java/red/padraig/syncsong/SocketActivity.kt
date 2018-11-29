package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_socket.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)

        socket_btn_connect.setOnClickListener { connect() }
    }

    private fun connect() {
        Log.d("SocketActivity", "Connecting...")
        // TODO could use javax websockets here instead
        val socket = object : WebSocketClient(URI("http://padraig.red:8080/ws")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d("SocketActivity", "Opened")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d("SocketActivity", "Closed")
            }

            override fun onMessage(message: String?) {
                Log.d("SocketActivity", "Message received: $message")
            }

            override fun onError(ex: Exception?) {
                Log.d("SocketActivity", "Error: $ex")
            }

        }
        socket.connect()
//        val socket = IO.socket("http://padraig.red:8080/ws")
//        socket.on(Socket.EVENT_CONNECT) {
//            socket.emit("foo", "hi")
//            Log.d("SocketActivity", "Connected")
//
//        }
//                .on(Socket.EVENT_CONNECT_ERROR) {
//                    Log.d("SocketActivity", "Connection error: ${it[0]}")
//
//                }
//                .on(Socket.EVENT_ERROR) {
//                    Log.d("SocketActivity", "error: ${it[0]}")
//
//                }
//                .on("data") { args ->
//                    val obj = args[0] as JSONObject
//                    Log.d("SocketActivity", obj.toString())
//                }
//        socket.connect()
    }
}
