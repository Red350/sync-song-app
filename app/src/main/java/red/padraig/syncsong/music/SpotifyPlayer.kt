package red.padraig.syncsong.music

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.spotify.android.appremote.api.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import red.padraig.syncsong.R
import red.padraig.syncsong.data.MyTrack
import red.padraig.syncsong.tag


class SpotifyPlayer(val context: Context, val trackState: Channel<MyTrack>) : MusicPlayer {

    private var spotifyAppRemote: SpotifyAppRemote? = null

    // These being lateinit will cause issues if any function is called before connect resolves.
    private lateinit var playerApi: PlayerApi
    private lateinit var imagesApi: ImagesApi

    private lateinit var currentTrack: MyTrack

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
                        this@SpotifyPlayer.spotifyAppRemote = spotifyAppRemote
                        playerApi = spotifyAppRemote.playerApi
                        imagesApi = spotifyAppRemote.imagesApi

                        // Create a listener for track state. This sends tracks back to the instantiator
                        // via the trackState channel.
                        this@SpotifyPlayer.spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                            val track = it.track
                            if (track != null) {
                                GlobalScope.launch {
                                    if (track.name == null) {
                                        Log.e(this.tag(), "Track name is null: $track, ${track.uri}")
                                    }
                                    currentTrack = MyTrack(track.uri ?: "", track.name ?: "", track.artist.name ?: "", -1, "", track.imageUri, null)
                                    trackState.send(currentTrack)
                                }
                            }
                        }
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e(this@SpotifyPlayer.tag(), throwable.message, throwable)
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

    override fun seekTo(pos: Long) {
        Log.d(this.tag(), "Seeking to $pos")
        playerApi.seekTo(pos)
    }

    override fun seekToRelativePosition(pos: Long) {
        Log.d(this.tag(), "Seeking forward by $pos")
        playerApi.seekToRelativePosition(pos)
    }

    override fun skipNext() {
        Log.d(this.tag(), "Skipping to next song")
        playerApi.skipNext()
    }

    override fun queue(uri: String) {
        Log.d(this.tag(), "Adding track to queue: $uri")
        playerApi.queue(uri)
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
