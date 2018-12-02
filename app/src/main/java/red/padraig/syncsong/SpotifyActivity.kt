package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.android.synthetic.main.activity_spotify.*


class SpotifyActivity : AppCompatActivity() {

    companion object {
        private val CLIENT_ID = "8a5ddeeff2b14f95930f7c1b30d5a83b"
        private val REDIRECT_URI = "red.padraig.syncsong://callback"
    }

    private var mSpotifyAppRemote: SpotifyAppRemote? = null
    private var playing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_spotify)

        spotify_btn_playpause.setOnClickListener { togglePlay() }
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
                        Log.d("SpotifyActivity", "Connected! Yay!")

                        mSpotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                            val track = it.track
                            if (track != null) {
                                spotify_tv_track.text = (track.name + " by " + track.artist.name)
                            }
                        }

                        spotify_btn_playpause.isEnabled = true
                    }

                    override fun onFailure(throwable: Throwable) {
                        Log.e("SpotifyActivity", throwable.message, throwable)

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                })
    }

    override fun onStop() {
        super.onStop()
        SpotifyAppRemote.disconnect(mSpotifyAppRemote)

    }

    private fun togglePlay() {
        if (playing) {
            mSpotifyAppRemote?.playerApi?.pause()
        } else {
            mSpotifyAppRemote?.playerApi?.play("spotify:track:5ZrrXIYTvjXPKVQMjqaumR")
        }
        playing = !playing
    }
}
