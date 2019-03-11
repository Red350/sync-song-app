package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_lobby_list.*
import red.padraig.syncsong.Lobby
import red.padraig.syncsong.R
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.LobbyAdapter

class LobbyListActivity : AppCompatActivity() {

    private val lobbyList = mutableListOf<Lobby>()
    private lateinit var lobbyAdapter: LobbyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_list)

        lobbylist_btn_createlobby.setOnClickListener { startActivity(Intent(this, CreateLobbyActivity::class.java)) }

        lobbyAdapter = LobbyAdapter(this, lobbyList)
        lobbylist_lv_lobbies.adapter = lobbyAdapter
        lobbylist_lv_lobbies.setOnItemClickListener { _, _, i, _ ->
            joinLobby(lobbyList[i].id)
        }

        // TODO replace this with pull down to refresh
        getlobbies.setOnClickListener { getLobbies() }
    }

    override fun onResume() {
        super.onResume()

        // TODO add a loading icon until this completes
        getLobbies()
    }

    private fun getLobbies() {
        val queue = Volley.newRequestQueue(this)
        val url = getString(R.string.api_url_1) + getString(R.string.api_port) + getString(R.string.api_endpoint_lobbies)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(this.tag(), "List lobbies response: $response")
                    lobbyList.clear()
                    val parser = JsonParser()
                    val jObject = parser.parse(response.toString()) as JsonObject
                    jObject.entrySet().forEach {
                        lobbyList.add(Lobby(
                                it.key,
                                it.value.asJsonObject["name"].asString,
                                it.value.asJsonObject["genre"].asString,
                                it.value.asJsonObject["numMembers"].asInt,
                                it.value.asJsonObject["public"].asBoolean
                        ))
                    }
                    lobbyAdapter.notifyDataSetChanged()
                },
                Response.ErrorListener { error ->
                    Toast.makeText(this, "Error getting lobbies: ${String(error.networkResponse.data)}", Toast.LENGTH_LONG).show()
                }
        )
        queue.add(jsonObjectRequest)
    }

    private fun joinLobby(id: String) {
        val intent = Intent(this, LobbyActivity::class.java)
        intent.putExtra("LOBBY_ID", id)
        startActivity(intent)
    }
}
