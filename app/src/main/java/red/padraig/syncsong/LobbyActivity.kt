package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.android.synthetic.main.activity_lobby.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import red.padraig.syncsong.extensions.tag
import java.net.URI

class LobbyActivity : AppCompatActivity() {

    companion object {
        private const val CLIENT_ID = "8a5ddeeff2b14f95930f7c1b30d5a83b"
        private const val REDIRECT_URI = "red.padraig.syncsong://callback"
    }

    private lateinit var socket: WebSocketClient
    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var playing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        connectToServer()

        lobby_btn_connect.setOnClickListener { connectToServer() }
        lobby_btn_send.setOnClickListener {
            socket.send(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }
        lobby_btn_playpause.setOnClickListener { togglePlay() }

    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(LobbyActivity.CLIENT_ID)
                .setRedirectUri(LobbyActivity.REDIRECT_URI)
                .showAuthView(true)
                .build()

        SpotifyAppRemote.connect(this, connectionParams,
                object : Connector.ConnectionListener {

                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote
                        Log.d(this@LobbyActivity.tag(), "Connected to Spotify")

                        mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                            val track = it.track
                            if (track != null) {
                                lobby_tv_track.text = (track.name + " by " + track.artist.name)
                            }
                        }

                        lobby_btn_playpause.isEnabled = true
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(this@LobbyActivity.tag(), throwable.message, throwable)
                    }
                })
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)

    }

    private fun connectToServer() {
        socket = object : WebSocketClient(URI("http://padraig.red:8080/ws")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(this@LobbyActivity.tag(), "Opened")
                setConnectionState(true)
                setStatus("Connected")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(this@LobbyActivity.tag(), "Closed")
                setConnectionState(false)
                setStatus("Closed")
            }

            override fun onMessage(message: String?) {
                Log.d(this@LobbyActivity.tag(), "Message received: $message")
                parseMessage(message.toString())
            }

            override fun onError(ex: Exception?) {
                Log.d(this@LobbyActivity.tag(), "Error: $ex")
                setConnectionState(false)
                setStatus("Error: $ex")
            }
        }
        socket.connect()
    }

    private fun togglePlay() {
        if (playing) {
            socket.send("play")
        } else {
            socket.send("pause")
        }
    }

    private fun play() {
        mSpotifyAppRemote?.playerApi?.play("spotify:track:5ZrrXIYTvjXPKVQMjqaumR")
        playing = true
    }

    private fun pause() {
        mSpotifyAppRemote?.playerApi?.pause()
        playing = false
    }

    private fun setConnectionState(connected: Boolean) {
        this.runOnUiThread {
            lobby_btn_connect.visibility = if (connected) View.INVISIBLE else View.VISIBLE
            lobby_btn_send.isEnabled = connected
        }
    }

    private fun setStatus(status: String) {
        this.runOnUiThread { lobby_tv_status.text = " ($status)" }
    }


    private fun displayMessage(msg: String) {
        this.runOnUiThread { lobby_tv_messages.append(msg + "\n") }
    }

    private fun parseMessage(msg: String) {
        when {
            msg.contains("play") -> play()
            msg.contains("pause") -> pause()
            else -> displayMessage(msg)
        }
    }
}
