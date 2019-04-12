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
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import red.padraig.syncsong.R
import red.padraig.syncsong.data.SSTrack
import red.padraig.syncsong.escapeSpecialCharacters
import red.padraig.syncsong.music.MusicPlayer
import red.padraig.syncsong.music.SpotifyPlayer
import red.padraig.syncsong.network.*
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.adapter.QueueAdapter
import red.padraig.syncsong.unescapeSpecialCharacters
import java.net.URI
import java.net.URLEncoder
import java.util.*
import kotlin.concurrent.schedule

class LobbyActivity : BaseActivity() {

    companion object {
        const val SEARCH_REQUEST_CODE = 1
        const val CLIENT_REQUEST_CODE = 2
        // This is static to prevent a user from connecting to multiple lobbies at once.
        private var socket: WebSocketClient? = null
    }

    private lateinit var lobbyID: String
    private lateinit var lobbyName: String
    private lateinit var admin: String
    private lateinit var clientNames: Array<String>
    private lateinit var clockHandshake: ClockHandshake

    private val playerState = Channel<SSTrack>()
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var mostRecentTrack: SSTrack

    private var queueOpen = false
    private val queueList = mutableListOf<SSTrack>()
    private lateinit var queueAdapter: QueueAdapter

    private var optionsMenu: Menu? = null

    private var spotifyConnected = false
    private var handshakeComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby)

        // Create and connect to Spotify music player.
        musicPlayer = SpotifyPlayer(applicationContext, playerState, this::setSpotifyConnectionState)
        musicPlayer.connect()
        subscribeToPlayerState()

        initListeners()

        lobbyID = intent.getStringExtra("LOBBY_ID")
        lobbyName = intent.getStringExtra("LOBBY_NAME")
        initialiseActionBar(lobbyName)

        // Connect to the lobby if we do not hold on open connection, or if the open connection is
        // to a different lobby. Otherwise, since we are connected, enable the UI.
        if (socket == null || socket?.isClosed == true || sharedPrefs.lobbyID != lobbyID) {
            // Disconnect from the previous lobby if necessary.
            // It does not cause any issues to call close() on an already closed socket.
            socket?.close(CloseFrame.SERVICE_RESTART, "User joining a new lobby")

            joinLobby()
        }

        // Initialise the handshake object.
        clockHandshake = ClockHandshake(socket) {
            Log.d(this.tag(), "Handshake complete")
            runOnUiThread {
                handshakeComplete = true
                enableUI()
                requestLobbyState()
            }
        }
    }

    private fun initListeners() {
        queueAdapter = QueueAdapter(this, queueList)
        lobby_lv_queue.adapter = queueAdapter

        lobby_btn_send.setOnClickListener {
            sendUserMessage(lobby_et_message.text.toString())
            lobby_et_message.setText("")
        }

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

    // Minimise the app if back pressed while in a lobby.
    // Users can still leave the lobby through the action bar.
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_lobby, menu)
        optionsMenu = menu
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.lobby_menuitem_search -> {
            startActivityForResult(Intent(this, SearchActivity::class.java), SEARCH_REQUEST_CODE)
            true
        }
        R.id.lobby_menuitem_clients -> {
            val intent = Intent(this, ClientListActivity::class.java)
            intent.putExtra("ADMIN", admin)
            intent.putExtra("CLIENTS", clientNames)
            startActivityForResult(intent, CLIENT_REQUEST_CODE)
            true
        }
        R.id.lobby_menuitem_exit -> {
            userLeaveLobby()
            toastShort("Exited lobby")
            true
        }
        R.id.lobby_menuitem_resync -> {
            requestLobbyState()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) return

        when (requestCode) {
            SEARCH_REQUEST_CODE -> {
                // Send a request to the server to add this song to the queue.
                val searchTrack = data.getParcelableExtra<SSTrack>("TRACK")

                sendMessage(Message(track = searchTrack, command = ClientCommand.AddSong.ordinal))
            }
            CLIENT_REQUEST_CODE -> {
                // Send a request to the server to promote the provided client.
                sendMessage(Message(admin = data.getStringExtra("ADMIN"), command = ClientCommand.Promote.ordinal))
            }
            else -> return
        }

    }

    // Connect to the Sync Song server via websocket and initialise a message listener.
    private fun joinLobby() {
        // Encode username.
        val lobbyURI = "${sharedPrefs.syncSongURL}${getString(R.string.api_port)}/lobbies/$lobbyID/join?username=${URLEncoder.encode(sharedPrefs.username, "UTF-8")}"
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

                when (code) {
                    CloseFrame.SERVICE_RESTART -> {
                        Log.d(this@LobbyActivity.tag(), "Connecting to new lobby")
                    }
                    CloseFrame.REFUSE -> {
                        Log.d(this@LobbyActivity.tag(), "Lobby does not exist")
                        this@LobbyActivity.runOnUiThread {
                            Toast.makeText(this@LobbyActivity, "Lobby does not exist", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                    else -> {
                        // Inform the user if the server dropped the connection.
                        if (remote) {
                            toastShort("Lost connection to lobby")
                        }
                        setConnectionState(false)
                    }
                }
            }

            override fun onMessage(jsonMessage: String?) {
                Log.d(this@LobbyActivity.tag(), "Message received: $jsonMessage")
                // The message is converted into a Message object before parsing.
                val message = Message.unmarshal(jsonMessage)
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
                // TODO admin read when the track changes, stop the next track and send a track finished
                // message to the server.
                val currentTrack = playerState.receive()
                mostRecentTrack = currentTrack
                setCurrentlyPlayingUI(currentTrack)
            }
        }
    }

    private fun enableUI() {
        lobby_btn_send.isEnabled = true
        lobby_btn_voteskip.isEnabled = true
        optionsMenu?.findItem(R.id.lobby_menuitem_exit)?.isEnabled = true
        optionsMenu?.findItem(R.id.lobby_menuitem_search)?.isEnabled = true
        optionsMenu?.findItem(R.id.lobby_menuitem_clients)?.isEnabled = true
        optionsMenu?.findItem(R.id.lobby_menuitem_resync)?.isEnabled = true
    }

    // Display details of the song currently playing.
    private fun setCurrentlyPlayingUI(track: SSTrack) {
        runOnUiThread {
            rowtrack_tv_name.text = track.name
            rowtrack_tv_artist.text = track.artist
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

    private fun updateTrackQueueUI(tracks: Array<SSTrack>?) {
        // Since the queue is sent with every message, a null queue is considered empty.
        queueList.clear()
        if (tracks != null) {
            queueList.addAll(tracks)
        }
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

    // Displays the connection state.
    private fun setConnectionState(connected: Boolean) {
        runOnUiThread {
            supportActionBar?.subtitle = lobbyID + " | " + if (connected) "Connected" else "Disconnected"

            // Leave the lobby if the websocket connection is lost.
            if (!connected) {
                leaveDueToServerDisconnect()
            }
        }
    }

    private fun setSpotifyConnectionState(connected: Boolean) {
        spotifyConnected = connected
        if (connected) {
            Log.d(this.tag(), "Connected to Spotify")
            requestLobbyState()
        } else {
            Log.d(this.tag(), "Lost connection to Spotify, reconnecting...")
            musicPlayer.connect()
        }
    }

    private fun displayUserMessage(msg: Message) {
        runOnUiThread {
            lobby_tv_messages.append(if (msg.username == null) {
                "\n" + msg.userMsg
            } else {
                "\n${msg.username}: ${msg.userMsg?.unescapeSpecialCharacters()}"
            })
        }
    }

    // Look at which fields in the message are set and response appropriately.
    private fun processMessage(msg: Message) {
        Log.d(this.tag(), "Processing message: $msg")
        // Process handshake message.
        if (msg.command != null && getServerCommandByOrdinal(msg.command) == ServerCommand.Handshake) {
            Log.d(this.tag(), "Received Handshake command")
            clockHandshake.parseMessage(msg)
            return
        }

        // If userMsg contains something, then the rest of the message does not carry state so we return.
        if (msg.userMsg != null) {
            displayUserMessage(msg)
            return
        }

        // Update the admin if set.
        if (msg.admin != null) {
            admin = msg.admin
        }

        // Update the clients if set.
        if (msg.clientNames != null) {
            clientNames = msg.clientNames
        }

        // Update the queue.
        updateTrackQueueUI(msg.trackQueue)

        // If no command set, ensure song state matches server.
        if (msg.command == null) {
            Log.d(this.tag(), "Received no command, checking song state")
            if (msg.track == null) {
                musicPlayer.pause()
            } else if (msg.track.uri != mostRecentTrack.uri) {
                seekTo(msg.track, msg.timestamp)
            }
        } else {
            val command = getServerCommandByOrdinal(msg.command)
            // Distinguish between commands that require the track to be set in the message.
            if (msg.track != null) {
                when (command) {
                    ServerCommand.Play -> {
                        Log.d(this.tag(), "Scheduling Play command for ${msg.timestamp}: " +
                                "duration: ${msg.track.duration}, position: ${msg.track.position}")
                        Timer("Play", false).schedule(Date(msg.timestamp)) {
                            Log.d(this@LobbyActivity.tag(), "Play command executed")
                            musicPlayer.play(msg.track.uri)
                        }
                        return
                    }
                    ServerCommand.Skip -> {
                        Log.d(this.tag(), "Received Skip command")
                        musicPlayer.play(msg.track.uri)
                        return
                    }
                    ServerCommand.SeekTo -> {
                        Log.d(this.tag(), "Received SeekTo command")
                        seekTo(msg.track, msg.timestamp)
                        return
                    }
                    ServerCommand.SeekRelative -> {
                        Log.d(this.tag(), "Received SeekRelative command")
                        musicPlayer.seekToRelativePosition(msg.track.position)
                        return
                    }
                    else -> Unit
                }

            }

            when (command) {
                ServerCommand.Pause -> {
                    Log.d(this.tag(), "Received Pause command")
                    return
                }
                ServerCommand.Resume -> {
                    Log.d(this.tag(), "Received Resume command")
                    musicPlayer.resume()
                    return
                }
                ServerCommand.Queue -> {
                    Log.d(this.tag(), "Received Queue command")
                    // Do nothing, spotify queue can't be relied on due to there being no method
                    // to clear it in the api.
                    return
                }
                else -> Log.e(this.tag(), "Invalid command: ${msg.command}")
            }
        }
    }

    private fun seekTo(track: SSTrack, timestamp: Long) {
        Log.d(this.tag(), "Playing track in preparation for seek")
        musicPlayer.play(track.uri)
        Log.d(this.tag(), "Scheduling SeekTo command for $timestamp: duration: ${track.duration}, position: ${track.position}")
        Timer("SeekTo", false).schedule(Date(timestamp)) {
            Log.d(this@LobbyActivity.tag(), "SeekTo command executed")
            musicPlayer.seekTo(track.position)
        }
    }

    private fun requestLobbyState() {
        if (handshakeComplete && spotifyConnected) {
            Log.d(this.tag(), "Requesting lobby state")
            sendMessage(Message(command = ClientCommand.State.ordinal))
        }
    }

    private fun voteSkip() {
        Log.d(this.tag(), "Voting to skip")
        sendMessage(Message(command = ClientCommand.VoteSkip.ordinal))
        toastShort("Vote cast")
    }

    private fun sendUserMessage(userMsg: String) {
        Log.d(this.tag(), "Sending user message")
        sendMessage(Message(userMsg = userMsg.escapeSpecialCharacters()))
    }

    // Adds the username to the message and sends it.
    private fun sendMessage(msg: Message) {
        msg.username = sharedPrefs.username
        Log.d(this.tag(), "Sending message: $msg")
        try {
            socket?.send(Message.marshal(msg))
        } catch (e: WebsocketNotConnectedException) {
            Log.e(this.tag(), "Lost connection to lobby")
            toastShort("Lost connection to lobby")
            leaveDueToServerDisconnect()
        }
    }

    private fun leaveDueToServerDisconnect() {
        Log.e(this.tag(), "Leaving due to server disconnect")
        if (spotifyConnected) {
            musicPlayer.pause()
        }
        finish()
    }

    private fun userLeaveLobby() {
        Log.e(this.tag(), "Leaving due to user request")
        if (spotifyConnected) {
            musicPlayer.pause()
        }
        socket?.close(CloseFrame.NORMAL, "User has left the lobby")
        finish()
    }
}
