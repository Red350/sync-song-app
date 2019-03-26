package red.padraig.syncsong.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Lobby
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag

// Contains methods to call Sync Song server API endpoints.
class SyncSongAPI(private val context: Context, private val volleyQueue: RequestQueue) {

    // Send a get lobbies request, and return the response through the provided callback.
    fun getLobbies(callback: (MutableList<Lobby>) -> Unit) {
        val url = context.getString(R.string.api_url_1) + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_lobbies)
        val lobbyList = mutableListOf<Lobby>()

        Log.d(this.tag(), "Sending get lobbies request")
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(this.tag(), "Get lobbies response: $response")
                    val parser = JsonParser()
                    val jObject = parser.parse(response.toString()) as JsonObject
                    jObject.entrySet().forEach {
                        val lobby = it.value.asJsonObject
                        lobbyList.add(Lobby(
                                it.key,
                                lobby["name"].asString,
                                lobby["genre"].asString,
                                lobby["numMembers"].asInt,
                                lobby["public"].asBoolean
                        ))
                    }
                    callback(lobbyList)
                },
                Response.ErrorListener { error ->
                    val errorMsg = "Error getting lobbies: ${error.printableError()}"
                    Log.d(this@SyncSongAPI.tag(), errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
        )
        volleyQueue.add(jsonObjectRequest)
    }

    fun createLobby(name: String, genre: String, public: Boolean, responseListener: Response.Listener<String>) {
        val url = context.getString(R.string.api_url_1) + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_createlobby)

        Log.d(this.tag(), "Sending create lobby request: $url")
        val createRequest = object : StringRequest(Request.Method.POST, url,
                responseListener,
                Response.ErrorListener { error ->
                    val errorMsg = "Error creating lobby: ${error.printableError()}"
                    Log.d(this@SyncSongAPI.tag(), errorMsg)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["genre"] = genre
                params["public"] = public.toString()

                return params
            }
        }

        volleyQueue.add(createRequest)
    }
}