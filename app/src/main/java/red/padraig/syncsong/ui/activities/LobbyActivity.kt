package red.padraig.syncsong.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.android.synthetic.main.activity_lobby.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import red.padraig.syncsong.R
import red.padraig.syncsong.escapeSpecialCharacters
import red.padraig.syncsong.tag
import red.padraig.syncsong.unescapeSpecialCharacters
import java.net.URI

class LobbyActivity : AppCompatActivity() {

    companion object {
        private const val CLIENT_ID = "8a5ddeeff2b14f95930f7c1b30d5a83b"
        private const val REDIRECT_URI = "red.padraig.syncsong://callback"
    }

    private lateinit var id: String
    private lateinit var socket: WebSocketClient
    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var playing = false

    private var titleRegex = Regex("""\{name: (.*)\}\"\}""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        id = intent.getStringExtra("LOBBY_ID")

        connectToServer()

        lobby_btn_connect.setOnClickListener { connectToServer() }
        lobby_btn_send.setOnClickListener {
            sendMessage(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }
        lobby_btn_playpause.setOnClickListener { togglePlay() }

    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                .setRedirectUri(REDIRECT_URI)
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
        socket = object : WebSocketClient(URI("http://padraig.red:8080/lobbies/$id/join")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(this@LobbyActivity.tag(), "Socket connection opened")
                setConnectionState(true)
                setStatus("Connected")
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(this@LobbyActivity.tag(), "Socket connection closed")
                if (code == 1003) {
                    Log.d(this@LobbyActivity.tag(), "Lobby does not exist")
                    this@LobbyActivity.runOnUiThread {
                        Toast.makeText(this@LobbyActivity, "Lobby does not exist", Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }

                }
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
            socket.send("{command: pause}")
        } else {
            socket.send("{command: play}")
        }
    }

    private fun play() {
        Log.d(this.tag(), "Playing")
        mSpotifyAppRemote?.playerApi?.play("spotify:track:5ZrrXIYTvjXPKVQMjqaumR")
        playing = true
    }

    private fun pause() {
        Log.d(this.tag(), "Pausing")
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

    private fun setLobbyName(name: String?) {
        Log.d(this.tag(), "Setting name to $name")
        this.runOnUiThread { lobby_tv_title.text = name }
    }

    private fun displayMessage(msg: String) {
        this.runOnUiThread { lobby_tv_messages.append(msg + "\n") }
    }

    private fun parseMessage(msg: String) {
        when {
            msg.contains("{command: play}") -> play()
            msg.contains("{command: pause}") -> pause()
            titleRegex.containsMatchIn(msg) -> {
                setLobbyName(titleRegex.find(msg)?.groupValues?.get(1))
            }
            else -> displayMessage(msg.unescapeSpecialCharacters())
        }
    }

    private fun sendMessage(msg: String) {
        socket.send(msg.escapeSpecialCharacters())
    }
}
