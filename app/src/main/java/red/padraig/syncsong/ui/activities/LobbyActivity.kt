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
import com.google.gson.Gson
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
import red.padraig.syncsong.network.ClientCommand
import red.padraig.syncsong.network.Message
import red.padraig.syncsong.network.ServerCommand
import red.padraig.syncsong.network.getServerCommandByOrdinal
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.adapater.QueueAdapter
import red.padraig.syncsong.unescapeSpecialCharacters
import java.net.URI
import java.net.URLEncoder

class LobbyActivity : BaseActivity() {

    companion object {
        const val SEARCH_REQUEST_CODE = 1
        // This is static to prevent a user from connecting to multiple lobbies at once.
        private var socket: WebSocketClient? = null
    }

    private lateinit var lobbyID: String
    private lateinit var lobbyName: String

    private val playerState = Channel<MyTrack>()
    private lateinit var musicPlayer: MusicPlayer

    private var playing = false
    private lateinit var currentTrack: MyTrack

    private var queueOpen = false
    private val queueList = mutableListOf<MyTrack>()
    private lateinit var queueAdapter: QueueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        initListeners()

        lobbyID = intent.getStringExtra("LOBBY_ID")
        lobbyName = intent.getStringExtra("LOBBY_NAME")
        initialiseActionBar(lobbyName)

        // Connect to the lobby if we do not hold on open connection, or if the open connection is
        // to a different lobby.
        if (socket == null || socket?.isClosed == true || sharedPrefs.lobbyID != lobbyID) {
            // Disconnect from the previous lobby if necessary.
            // It does not cause any issues to call close() on an already closed socket.
            socket?.close(CloseFrame.NORMAL, "User joining a new lobby")

            joinLobby()
        }

        // Create and connect to Spotify music player.
        musicPlayer = SpotifyPlayer(applicationContext, playerState)
        musicPlayer.connect()
        subscribeToPlayerState()
    }

    private fun initListeners() {
        queueAdapter = QueueAdapter(this, queueList)
        lobby_lv_queue.adapter = queueAdapter

        lobby_btn_send.setOnClickListener {
            sendUserMessage(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }

        lobby_btn_playpause.setOnClickListener { togglePlay() }

        lobby_btn_voteskip.setOnClickListener { voteSkip() }

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

    override fun onDestroy() {
        super.onDestroy()
        musicPlayer.disconnect()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != SEARCH_REQUEST_CODE) return

        if (data != null) {
            // TODO add this to a queue based on lobby mode
            val searchTrack = MyTrack(
                    data.getStringExtra("TRACK_URI"),
                    data.getStringExtra("TRACK_NAME"),
                    data.getStringExtra("TRACK_ARTIST"),
                    -1,
                    sharedPrefs.username,
                    null,
                    null
            )

            sendMessage(Message(null, searchTrack, null, ClientCommand.AddSong.ordinal, null))
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
        // Encode username.
        val lobbyURI = "http://padraig.red:8080/lobbies/$lobbyID/join?username=${URLEncoder.encode(sharedPrefs.username, "UTF-8")}"
        Log.d(this.tag(), "Connecting to lobby: $lobbyURI")
        socket = object : WebSocketClient(URI(lobbyURI)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(this@LobbyActivity.tag(), "Socket connection opened")
                sharedPrefs.lobbyID = lobbyID
                setConnectionState(true)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.e(this@LobbyActivity.tag(), "Socket connection closed by${if (remote) " host" else " client"}: $code: $reason")
                sharedPrefs.lobbyID = ""
                if (code == CloseFrame.REFUSE) {
                    Log.d(this@LobbyActivity.tag(), "Lobby does not exist")
                    this@LobbyActivity.runOnUiThread {
                        Toast.makeText(this@LobbyActivity, "Lobby does not exist", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } else {
                    setConnectionState(false)
                }
            }

            override fun onMessage(jsonMessage: String?) {
                Log.d(this@LobbyActivity.tag(), "Message received: $jsonMessage")
                // The message is converted into a Message object before parsing.
                val message = unmarshal(jsonMessage)
                processMessage(message)
            }

            override fun onError(ex: Exception?) {
                Log.e(this@LobbyActivity.tag(), "Error: $ex")
            }
        }
        socket?.connect()
    }

    private fun subscribeToPlayerState() {
        GlobalScope.launch {
            while (true) {
                setCurrentlyPlayingUI(playerState.receive())
            }
        }
    }

    // Display details of the song currently playing.
    private fun setCurrentlyPlayingUI(track: MyTrack) {
        runOnUiThread {
            rowtrack_tv_name.text = track.name
            rowtrack_tv_artist.text = track.artist

            // TODO this shouldn't be in the final app, but for now this seems like the best place to enable it.
            lobby_btn_playpause.isEnabled = true
        }

        // Don't think it makes a difference, but probably best not to run this on the UI thread
        // since it's an asynchronous call itself.
        musicPlayer.getCurrentImage { setTrackImage(it) }
    }

    private fun setTrackImage(image: Bitmap?) {
        runOnUiThread {
            if (image == null) {
                rowtrack_iv_artwork.setImageDrawable(getDrawable(R.drawable.ic_broken_image_black_64dp))
            } else {
                rowtrack_iv_artwork.setImageBitmap(image)
            }
        }
    }

    private fun updateTrackQueueUI(tracks: Array<MyTrack>?) {
        // Since the queue is sent with every message, a null queue is considered empty.
        queueList.clear()
        if (tracks == null) return
        queueList.addAll(tracks)
        runOnUiThread {
            queueAdapter.notifyDataSetChanged()
        }
    }

    // Hides or shows the song queue UI.
    private fun toggleDisplaySongQueue() {
        if (queueOpen) {
            // Closing the queue.
            lobby_lv_queue.visibility = View.VISIBLE
            lobby_lv_queue.alpha = 1.0f
            lobby_lv_queue.animate().alpha(0.0f).withEndAction {
                lobby_lv_queue.visibility = View.GONE
            }

            // Rotate the chevron.
            lobby_iv_queuechevron.animate().rotation(0.0f)
        } else {
            // Opening the queue.
            lobby_lv_queue.visibility = View.VISIBLE
            lobby_lv_queue.alpha = 0.0f
            lobby_lv_queue.animate().alpha(1.0f)

            // Rotate the chevron.
            lobby_iv_queuechevron.animate().rotation(180.0f)
        }

        queueOpen = !queueOpen
    }

    private fun togglePlay() {
        if (playing) {
            socket?.send("{command: pause}")
        } else {
            socket?.send("{command: play}")
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
    private fun setConnectionState(connected: Boolean) {
        runOnUiThread {
            lobby_btn_send.isEnabled = connected
            supportActionBar?.subtitle = lobbyID + " | " + if (connected) "Connected" else "Disconnected"
        }
    }

    private fun displayUserMessage(msg: Message) {
        runOnUiThread { lobby_tv_messages.append("${msg.username}: ${msg.userMsg?.unescapeSpecialCharacters()}\n") }
    }

    // Look at which fields in the message are set and response appropriately.
    private fun processMessage(msg: Message) {
        // Display user message if set.
        if (msg.userMsg != null) {
            displayUserMessage(msg)
        }

        // Update the queue.
        updateTrackQueueUI(msg.trackQueue)

        // Execute command if set.
        if (msg.command != null) {
            val command = getServerCommandByOrdinal(msg.command)
            // Distinguish between commands that require the track to be set in the message.
            if (msg.track != null) {
                when (command) {
                    ServerCommand.Play -> {
                        musicPlayer.play(msg.track.uri)
                        currentTrack = msg.track
                        return
                    }
                    ServerCommand.Skip -> {
                        musicPlayer.play(msg.track.uri)
                        currentTrack = msg.track
                        return
                    }
                    ServerCommand.SeekTo -> {
                        musicPlayer.seekTo(msg.track.position)
                        return
                    }
                    ServerCommand.SeekRelative -> {
                        musicPlayer.seekToRelativePosition(msg.track.position)
                        return
                    }
                    else -> Unit
                }

            }

            when (command) {
                ServerCommand.Pause -> {
                    musicPlayer.pause()
                    return
                }
                ServerCommand.Resume -> {
                    musicPlayer.resume()
                    return
                }
                ServerCommand.Queue -> {
                    if (msg.trackQueue == null) return
                    updateTrackQueueUI(msg.trackQueue)

                    // Add first track to the queue.
                    if (!msg.trackQueue.isEmpty()) {
                        musicPlayer.queue(msg.trackQueue[0].uri)
                    }
                    return
                }
                else -> Log.e(this.tag(), "Invalid command: ${msg.command}")
            }
        }
    }

    private fun marshal(msg: Message): String = Gson().toJson(msg)

    private fun unmarshal(jsonMessage: String?): Message = Gson().fromJson<Message>(jsonMessage, Message::class.java)

    private fun voteSkip() {
        sendMessage(Message(null, null, null, ClientCommand.VoteSkip.ordinal, null))
        toastShort("Vote cast")
    }

    private fun sendUserMessage(userMsg: String) {
        sendMessage(Message(null, null, null, null, userMsg.escapeSpecialCharacters()))
    }

    // Adds the username to the message and sends it.
    private fun sendMessage(msg: Message) {
        msg.username = sharedPrefs.username
        Log.d(this.tag(), "Sending message: $msg")
        socket?.send(marshal(msg))
    }
}
