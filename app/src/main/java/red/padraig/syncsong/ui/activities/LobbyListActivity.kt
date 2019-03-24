package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_lobby_list.*
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Lobby
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.LobbyAdapter

class LobbyListActivity : BaseActivity() {

    private val lobbyList = mutableListOf<Lobby>()
    private lateinit var lobbyAdapter: LobbyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_list)

        lobbylist_btn_createlobby.setOnClickListener { startActivity(Intent(this, CreateLobbyActivity::class.java)) }

        // Display the refresh icon immediately.
        lobbylist_SRL_lobbies.isRefreshing = true

        // Initialise lobby list view.
        lobbyAdapter = LobbyAdapter(this, lobbyList)
        lobbylist_lv_lobbies.adapter = lobbyAdapter
        lobbylist_lv_lobbies.setOnItemClickListener { _, _, i, _ ->
            joinLobby(lobbyList[i].id)
        }

        // Initialise swipe to refresh.
        lobbylist_SRL_lobbies.setOnRefreshListener {
            lobbyList.clear()
            lobbyAdapter.notifyDataSetChanged()
            getLobbies()
        }

        // TODO replace this with pull down to refresh
        getlobbies.setOnClickListener { getLobbies() }
    }

    override fun onResume() {
        super.onResume()

        // TODO add a loading icon until this completes
        getLobbies()
    }

    // Request a list of lobbies from the server.
    // On response, this updates and notifies the lobbyAdatper.
    private fun getLobbies() {
        val url = getString(R.string.api_url_1) + getString(R.string.api_port) + getString(R.string.api_endpoint_lobbies)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(this.tag(), "List lobbies response: $response")
                    lobbyList.clear()
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
                    lobbyAdapter.notifyDataSetChanged()
                    // Hide the refreshing icon displayed by the SwipeRefreshLayout.
                    lobbylist_SRL_lobbies.isRefreshing = false
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error getting lobbies: ${error.printableError()}", Toast.LENGTH_LONG).show()
                }
        )
        volleyQueue.add(jsonObjectRequest)
    }

    private fun joinLobby(id: String) {
        val intent = Intent(this, LobbyActivity::class.java)
        intent.putExtra("LOBBY_ID", id)
        startActivity(intent)
    }
}
