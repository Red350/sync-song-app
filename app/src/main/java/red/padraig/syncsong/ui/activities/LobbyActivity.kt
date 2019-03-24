package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.row_track.*
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Track
import red.padraig.syncsong.escapeSpecialCharacters
import red.padraig.syncsong.tag
import red.padraig.syncsong.unescapeSpecialCharacters
import java.net.URI

class LobbyActivity : BaseActivity() {

    companion object {
        const val SEARCH_REQUEST_CODE = 1
    }
    private lateinit var id: String
    private lateinit var lobbyName: String
    private lateinit var socket: WebSocketClient
    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var playing = false
    private lateinit var currentTrack: Track

    private var titleRegex = Regex("""\{name: (.*)\}\"\}""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        id = intent.getStringExtra("LOBBY_ID")
        lobbyName = intent.getStringExtra("LOBBY_NAME")

        initialiseActionBar(lobbyName)

        connectToServer()

        lobby_btn_send.setOnClickListener {
            sendMessage(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }
        lobby_btn_playpause.setOnClickListener { togglePlay() }
    }

    override fun onStart() {
        super.onStart()
        val connectionParams = ConnectionParams.Builder(getString(R.string.CLIENT_ID))
                .setRedirectUri(getString(R.string.REDIRECT_URI))
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
                                setCurrentTrackUI(track)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != SEARCH_REQUEST_CODE) return

        if (data != null) {
            val track = data.getParcelableExtra<Track>("track")
            // TODO add this to a queue based on lobby mode
            currentTrack = track
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_lobby, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when(item.itemId) {
        R.id.lobby_menuitem_search -> {
            startActivityForResult(Intent(this, SearchActivity::class.java), SEARCH_REQUEST_CODE)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun connectToServer() {
        socket = object : WebSocketClient(URI("http://padraig.red:8080/lobbies/$id/join")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(this@LobbyActivity.tag(), "Socket connection opened")
                setConnectionStateWithReconnect(true)
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
                setConnectionStateWithReconnect(false)
            }

            override fun onMessage(message: String?) {
                Log.d(this@LobbyActivity.tag(), "Message received: $message")
                parseMessage(message.toString())
            }

            override fun onError(ex: Exception?) {
                Log.d(this@LobbyActivity.tag(), "Error: $ex")
                setConnectionStateWithReconnect(false)
            }
        }
        socket.connect()
    }

    private fun setCurrentTrackUI(track: com.spotify.protocol.types.Track) {
        rowtrack_tv_name.text = track.name
        rowtrack_tv_artist.text = track.artist.name
        // TODO get the album art.
    }

    private fun togglePlay() {
        if (playing) {
            socket.send("{command: pause}")
        } else {
            socket.send("{command: play}")
        }
    }

    private fun play(uri: String) {
        Log.d(this.tag(), "Playing $uri")
        // URI example: spotify:track:5ZrrXIYTvjXPKVQMjqaumR
        mSpotifyAppRemote?.playerApi?.play(uri)
        playing = true
    }

    private fun pause() {
        Log.d(this.tag(), "Pausing")
        mSpotifyAppRemote?.playerApi?.pause()
        playing = false
    }

    // Displays the connection state, an attempts to reconnect to the server if disconnected.
    private fun setConnectionStateWithReconnect(connected: Boolean) {
        this.runOnUiThread {
            lobby_btn_send.isEnabled = connected
            supportActionBar?.subtitle = if (connected) "Connected" else "Disconnected"
            if (!connected) connectToServer()
        }
    }

    private fun displayMessage(msg: String) {
        this.runOnUiThread { lobby_tv_messages.append(msg + "\n") }
    }

    private fun parseMessage(msg: String) {
        when {
            msg.contains("{command: play}") -> play(currentTrack.uri)
            msg.contains("{command: pause}") -> pause()
            titleRegex.containsMatchIn(msg) -> {
                // TODO stop server from sending this message.
                // Pass for now, since we get name from the intent.
                // setLobbyName(titleRegex.find(msg)?.groupValues?.get(1))
            }
            else -> displayMessage(msg.unescapeSpecialCharacters())
        }
    }

    private fun sendMessage(msg: String) {
        socket.send(msg.escapeSpecialCharacters())
    }
}
