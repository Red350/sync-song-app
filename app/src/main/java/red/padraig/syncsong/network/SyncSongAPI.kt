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
import red.padraig.syncsong.SharedPrefsWrapper
import red.padraig.syncsong.data.Lobby
import red.padraig.syncsong.data.LobbyMode
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag

// Contains methods to call Sync Song server API endpoints.
class SyncSongAPI(private val context: Context, private val volleyQueue: RequestQueue, private val sharedPrefs: SharedPrefsWrapper) {

    // Recursive function that successively queries urls until one responds.
    // The responding URL is then stored in shared preferences.
    fun setURL(urlIndex: Int = 0, successCallback: (Boolean) -> Unit) {
        val urls = context.resources.getStringArray(R.array.api_urls)

        val url =  urls[urlIndex]
        val pingURL = url + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_lobbies)
        Log.d(this.tag(), "Pinging Syng Song URL: $urls")
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, pingURL, null,
                Response.Listener {
                    Log.d(this.tag(), "Found Sync Song URL: $url")
                    sharedPrefs.syncSongURL = urls[urlIndex]
                    successCallback(true)
                },
                Response.ErrorListener {
                    if (urlIndex == urls.size - 1) {
                        // No more IPs to check, return false.
                        successCallback(false)
                    } else {
                        // Recursively call this function with the next URL index.
                        setURL(urlIndex+1, successCallback)
                    }
                }
        )
        volleyQueue.add(jsonObjectRequest)
    }

    // Send a getLobby request, and return the response through the provided callback.
    fun getLobby(id: String, errorListener: Response.ErrorListener, successCallback: (Lobby) -> Unit) {
        val url = sharedPrefs.syncSongURL + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_lobbies) + "/$id"

        Log.d(this.tag(), "Sending getLobby request: $url")
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(this.tag(), "GetLobby response: $response")
                    val parser = JsonParser()
                    val lobbyJson = parser.parse(response.toString()) as JsonObject
                    val lobby = Lobby(
                            lobbyJson["id"].asString,
                            lobbyJson["name"].asString,
                            lobbyJson["genre"].asString,
                            lobbyJson["numMembers"].asInt,
                            lobbyJson["public"].asBoolean
                    )
                    successCallback(lobby)
                },
                errorListener
        )
        volleyQueue.add(jsonObjectRequest)
    }

    // Send a get lobbies request, and return the response through the provided callback.
    fun getLobbies(errorListener: Response.ErrorListener, successCallback: (MutableList<Lobby>) -> Unit) {
        val url = sharedPrefs.syncSongURL + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_lobbies)
        val lobbyList = mutableListOf<Lobby>()

        Log.d(this.tag(), "Sending get lobbies request: $url")
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
                    successCallback(lobbyList)
                },
                errorListener
        )
        volleyQueue.add(jsonObjectRequest)
    }

    fun createLobby(name: String, mode: LobbyMode, genre: String, public: Boolean, admin: String, responseListener: Response.Listener<String>) {
        val url = sharedPrefs.syncSongURL + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_createlobby)

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
                params["mode"] = mode.ordinal.toString()
                params["genre"] = genre
                params["public"] = public.toString()
                params["admin"] = admin

                return params
            }
        }

        volleyQueue.add(createRequest)
    }
}