package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_lobby.*
import kotlinx.android.synthetic.main.row_track.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import red.padraig.syncsong.R
import red.padraig.syncsong.data.MyTrack
import red.padraig.syncsong.escapeSpecialCharacters
import red.padraig.syncsong.music.MusicPlayer
import red.padraig.syncsong.music.SpotifyPlayer
import red.padraig.syncsong.tag
import red.padraig.syncsong.unescapeSpecialCharacters
import java.net.URI

class LobbyActivity : BaseActivity() {

    companion object {
        const val SEARCH_REQUEST_CODE = 1
    }

    private lateinit var lobbyID: String
    private lateinit var lobbyName: String

    private val playerState = Channel<MyTrack>()
    private lateinit var socket: WebSocketClient
    private lateinit var musicPlayer: MusicPlayer

    private var playing = false
    private lateinit var currentTrack: MyTrack

    private var queueOpen = false

    private var titleRegex = Regex("""\{name: (.*)\}\"\}""")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        initListeners()

        lobbyID = intent.getStringExtra("LOBBY_ID")
        lobbyName = intent.getStringExtra("LOBBY_NAME")
        initialiseActionBar(lobbyName)

        // Connect to Sync Song lobby server.
        joinLobby()

        // Create Spotify music player.
        musicPlayer = SpotifyPlayer(applicationContext, playerState)
    }

    private fun initListeners() {
        lobby_btn_send.setOnClickListener {
            sendMessage(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }

        lobby_btn_playpause.setOnClickListener { togglePlay() }

        lobby_LL_queuetitle.setOnClickListener { toggleDisplaySongQueue() }

        // Automatically scroll message view to show new messages.
        // TODO this will obviously make it difficult to scroll back to view messages when receiving
        // new messages. Not a huge priority though as I can't think of a trivial fix.
        lobby_tv_messages.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                lobby_scroll_messages.scrollTo(0, lobby_scroll_messages.bottom)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    override fun onStart() {
        super.onStart()
        musicPlayer.connect()
        subscribeToPlayerState()
    }

    override fun onStop() {
        super.onStop()
        musicPlayer.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != SEARCH_REQUEST_CODE) return

        if (data != null) {
            // TODO add this to a queue based on lobby mode
            currentTrack = MyTrack(
                    data.getStringExtra("TRACK_URI"),
                    data.getStringExtra("TRACK_NAME"),
                    data.getStringExtra("TRACK_ARTIST"),
                    null,
                    null
            )
        }

        // TODO this shouldn't be in the final app, but for now this seems like the best place to enable it.
        lobby_btn_playpause.isEnabled = true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_lobby, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.lobby_menuitem_search -> {
            startActivityForResult(Intent(this, SearchActivity::class.java), SEARCH_REQUEST_CODE)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Connect to the Sync Song server via websocket and initialise a message listener.
    private fun joinLobby() {
        socket = object : WebSocketClient(URI("http://padraig.red:8080/lobbies/$lobbyID/join")) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(this@LobbyActivity.tag(), "Socket connection opened")
                setConnectionStateWithReconnect(true)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(this@LobbyActivity.tag(), "Socket connection closed")
                if (code == CloseFrame.REFUSE) {
                    Log.d(this@LobbyActivity.tag(), "Lobby does not exist")
                    this@LobbyActivity.runOnUiThread {
                        Toast.makeText(this@LobbyActivity, "Lobby does not exist", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                setConnectionStateWithReconnect(false)
            }

            override fun onMessage(message: String?) {
                Log.d(this@LobbyActivity.tag(), "Message received: $message")
                parseMessage(message.toString())
            }

            override fun onError(ex: Exception?) {
                Log.e(this@LobbyActivity.tag(), "Error: $ex")
            }
        }
        socket.connect()
    }

    private fun subscribeToPlayerState() {
        GlobalScope.launch {
            while(true) {
                setCurrentlyPlayingUI(playerState.receive())
            }
        }
    }

    // Display details of the song currently playing.
    private fun setCurrentlyPlayingUI(track: MyTrack) {
        this.runOnUiThread {
            rowtrack_tv_name.text = track.name
            rowtrack_tv_artist.text = track.artist

            // TODO this shouldn't be in the final app, but for now this seems like the best place to enable it.
            lobby_btn_playpause.isEnabled = true
        }

        // Don't think it makes a difference, but probably best not to run this on the UI thread
        // since it's an asynchronous call itself.
        musicPlayer.getCurrentImage { setTrackImage(it) }
    }

    private fun setTrackImage(image: Bitmap) {
        rowtrack_iv_artwork.setImageBitmap(image)
    }

    // Hides or shows the song queue UI.
    private fun toggleDisplaySongQueue() {
        if (queueOpen) {
            // Closing the queue.
            lobby_queue.visibility = View.VISIBLE
            lobby_queue.alpha = 1.0f
            lobby_queue.animate().alpha(0.0f).withEndAction {
                lobby_queue.visibility = View.GONE
            }

            // Rotate the chevron.
            lobby_iv_queuechevron.animate().rotation(0.0f)
        } else {
            // Opening the queue.
            lobby_queue.visibility = View.VISIBLE
            lobby_queue.alpha = 0.0f
            lobby_queue.animate().alpha(1.0f)

            // Rotate the chevron.
            lobby_iv_queuechevron.animate().rotation(180.0f)
        }

        queueOpen = !queueOpen
    }

    private fun togglePlay() {
        if (playing) {
            socket.send("{command: pause}")
        } else {
            socket.send("{command: play}")
        }
    }

    private fun play(uri: String) {
        // URI example: spotify:track:5ZrrXIYTvjXPKVQMjqaumR
        musicPlayer.play(uri)
        playing = true
    }

    private fun pause() {
        Log.d(this.tag(), "Pausing")
        musicPlayer.pause()
        playing = false
    }

    // Displays the connection state, an attempts to reconnect to the server if disconnected.
    private fun setConnectionStateWithReconnect(connected: Boolean) {
        this.runOnUiThread {
            lobby_btn_send.isEnabled = connected
            supportActionBar?.subtitle = lobbyID + " | " + if (connected) "Connected" else "Disconnected"
            if (!connected) joinLobby()
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
