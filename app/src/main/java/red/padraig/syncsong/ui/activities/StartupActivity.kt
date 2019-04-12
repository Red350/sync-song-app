package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import red.padraig.syncsong.R
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag


class StartupActivity : BaseActivity() {

    companion object {
        const val REQUEST_CODE = 1337
    }

    private var spotifyAuthComplete = false
    private var urlSearchComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        // Find the current active Sync Song server.
        checkSyncSongURLs()

        authWithSpotify()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Process response from Spotify Auth, and request user details.
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthenticationResponse.Type.TOKEN -> {
                    sharedPrefs.token = response.accessToken
                    requestUserDetails()
                }
                AuthenticationResponse.Type.ERROR -> {
                    Log.e(this.tag(), "Error authenticating: ${response.error}")
                }
                else -> Log.d(this.tag(), "Unexpected response: ${response.type}")
            }
        }
    }

    // Check a list of URLs to find out which server is active.
    private fun checkSyncSongURLs() {
        syncSongAPI.setURL { succeeded ->
            urlSearchComplete = true
            if (!succeeded) {
                toastLong("Sync Song server not running")
            }
            openLobbyList()
        }
    }

    // Requests a Spotify access token for the current user.
    private fun authWithSpotify() {
        val builder = AuthenticationRequest.Builder(
                getString(R.string.CLIENT_ID),
                AuthenticationResponse.Type.TOKEN,
                getString(R.string.REDIRECT_URI))
        builder.setScopes(arrayOf("user-read-private"))
        val request = builder.build()
        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    // Request a user's details and on success start the lobby list activity.
    private fun requestUserDetails() {
        val userDetailsRequest = object : JsonObjectRequest(
                Request.Method.GET, getString(R.string.spotify_url_me),
                null,
                Response.Listener { response ->
                    Log.d(this.tag(), "User details response: $response")
                    val details = JsonParser().parse(response.toString()) as JsonObject
                    sharedPrefs.id = details["id"].asString
                    sharedPrefs.username = details["display_name"].asString
                    spotifyAuthComplete = true
                    openLobbyList()
                },
                Response.ErrorListener { error ->
                    Log.e(this.tag(), "Error getting user details: ${error.printableError()}")
                    toastLong("Unable to get user details: ${error.printableError()}")
                }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer ${sharedPrefs.token}"
                return headers
            }
        }

        Log.d(this.tag(), "Sending user details request")
        volleyQueue.add(userDetailsRequest)
    }

    private fun openLobbyList() {
        // Only open the lobby list once url searching and spotify auth have completed.
        if (spotifyAuthComplete && urlSearchComplete) {
            startActivity(Intent(this, LobbyListActivity::class.java))
        }
    }
}
