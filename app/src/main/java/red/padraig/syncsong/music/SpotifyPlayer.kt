package red.padraig.syncsong.music

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.spotify.android.appremote.api.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import red.padraig.syncsong.R
import red.padraig.syncsong.data.SSTrack
import red.padraig.syncsong.tag


class SpotifyPlayer(val context: Context, val playerState: Channel<SSTrack>, val onConnectCallback: (Boolean) -> Unit) : MusicPlayer {

    private var spotifyAppRemote: SpotifyAppRemote? = null

    // These being lateinit will cause issues if any function is called before connect resolves.
    private lateinit var playerApi: PlayerApi
    private lateinit var imagesApi: ImagesApi

    private lateinit var currentTrack: SSTrack

    private val connectionParams = ConnectionParams.Builder(context.getString(R.string.CLIENT_ID))
            .setRedirectUri(context.getString(R.string.REDIRECT_URI))
            .showAuthView(true)
            .build()

    // Connect to the spotify remote api and set up callback for player state.
    override fun connect() {
        Log.d(this.tag(), "Connecting to Spotify...")
        SpotifyAppRemote.connect(context, connectionParams,
                object : Connector.ConnectionListener {

                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        Log.d(this@SpotifyPlayer.tag(), "Connected to Spotify")
                        onConnectCallback(true) // Inform the activity that we have connected to spotify.
                        this@SpotifyPlayer.spotifyAppRemote = spotifyAppRemote
                        playerApi = spotifyAppRemote.playerApi
                        imagesApi = spotifyAppRemote.imagesApi
                        spotifyAppRemote.userApi

                        // Create a listener for track state. This sends tracks back to the instantiator
                        // via the playerState channel.
                        this@SpotifyPlayer.spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                            val track = it.track
                            if (track != null) {
                                GlobalScope.launch {
                                    if (track.name == null) {
                                        Log.e(this.tag(), "Track name is null: $track, ${track.uri}")
                                    }
                                    currentTrack = SSTrack(uri = track.uri ?: "", name = track.name
                                            ?: "", artist = track.artist.name
                                            ?: "", imageUri = track.imageUri, duration = track.duration)
                                    playerState.send(currentTrack)
                                }
                            }
                        }
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(this@SpotifyPlayer.tag(), throwable.message, throwable)
                        onConnectCallback(false) // Inform the activity that we have connected to spotify.
                    }
                })
    }

    // Disconnect from the spotify remote api.
    override fun disconnect() {
        Log.d(this.tag(), "Disconnecting from Spotify...")
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }

    // Play the song based on the provided uri.
    // We don't have to worry about setting the current track, spotify app remote will automatically
    // propagate that info once it starts playing.
    override fun play(uri: String) {
        Log.d(this.tag(), "Playing track: $uri")
        playerApi.play(uri)
    }

    override fun pause() {
        Log.d(this.tag(), "Pausing track")
        playerApi.pause()
    }

    override fun resume() {
        Log.d(this.tag(), "Resuming track")
        playerApi.resume()
    }

    // Spotify ignores a seek command unless a song is already playing, so this should only be called
    // after calling play. The reason we don't call play here is because this is usually called
    // after a delay, and playing also takes time to propagate. So by calling play before the delayed
    // call to this we can have a more reliable synchronisation.

    override fun seekTo(pos: Long) {
        // Every time we need to sleep to wait for the player state, we can increment this by the
        // sleep amount to ensure the final seek value is correct.
        var actualPos = pos

        var seeked = false
        while (!seeked) {
            playerApi.playerState.setResultCallback {
                Log.d(this.tag(), "Received player state: ${if (it.isPaused) "Pause" else "Playing"}")
                if (!it.isPaused) {
                    if (!seeked) {
                        Log.d(this.tag(), "Seeking to $pos")
                        playerApi.seekTo(actualPos)
                        seeked = true
                    }
                }
            }
            Thread.sleep(50)
            actualPos += 50
        }
    }

    // TODO if we ever use this we'll have to give it the same treatment as seekTo()
    override fun seekToRelativePosition(pos: Long) {
        TODO("Unimplemented")
//        Log.d(this.tag(), "Seeking forward by $pos")
//        playerApi.seekToRelativePosition(pos)
    }

    override fun skipNext() {
        Log.d(this.tag(), "Skipping to next song")
        playerApi.skipNext()
    }

    override fun queue(uri: String) {
        TODO("Unimplemented")
//        Log.d(this.tag(), "Adding track to queue: $uri")
        // Do nothing here. There's no way to clear the spotify queue through this api. We can't
        // rely on a user's queue being empty so queueing songs is pointless.
        // playerApi.queue(uri)
    }

    // Makes an async call to the spotify remote api, and returns that image through the provided callback.
    override fun getCurrentImage(callback: (Bitmap?) -> Unit) {
        if (currentTrack.imageUri != null) {
            imagesApi.getImage(currentTrack.imageUri).setResultCallback {
                callback(it)
            }
        } else {
            callback(null)
        }
    }
}
