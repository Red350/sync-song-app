package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_socket.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SocketActivity : AppCompatActivity() {

    private lateinit var socket: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_socket)
            
        socket_btn_connect.setOnClickListener {
            socket = object : WebSocketClient(URI("http://padraig.red:8080/ws")) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    Log.d("SocketActivity", "Opened")
                    setConnectionState(true)
                    setStatus("Connected")
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d("SocketActivity", "Closed")
                    setConnectionState(false)
                    setStatus("Closed")
                }

                override fun onMessage(message: String?) {
                    Log.d("SocketActivity", "Message received: $message")
                    updateMessage(message.toString())
                }

                override fun onError(ex: Exception?) {
                    Log.d("SocketActivity", "Error: $ex")
                    setStatus("Error: $ex")
                }
            }
            socket.connect()
        }
        socket_btn_send.setOnClickListener {
            socket.send(socket_et_message.text.toString())
            socket_et_message.setText("")
            socket_et_message.isFocused
        }
    }

    private fun setConnectionState(connected: Boolean) {
        this.runOnUiThread { socket_btn_send.isEnabled = connected }
    }

    private fun setStatus(status: String) {
        this.runOnUiThread { socket_tv_status.text = status }
    }

    private fun updateMessage(msg: String) {
        this.runOnUiThread { socket_tv_messages.append(msg + "\n") }
    }
}
