package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_prototype.*
import red.padraig.syncsong.R

class PrototypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_prototype)

        main_btn_joinlobby.setOnClickListener {
            val intent = Intent(this, LobbyActivity::class.java)
            intent.putExtra("LOBBY_ID", main_et_lobbyid.text.toString())
            startActivity(intent)
        }

        main_btn_createlobby.setOnClickListener { startActivity(Intent(this, CreateLobbyActivity::class.java)) }

        main_btn_viewlobbies.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://padraig.red:8080/lobbies"

            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener { response ->
                        // Pretty printing courtesy of https://stackoverflow.com/a/50467797
                        val parser = JsonParser()
                        val json = parser.parse(response.toString())
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val prettyJson = gson.toJson(json)
                        main_tv_lobbies.text = prettyJson
                    },
                    Response.ErrorListener { error ->
                        main_tv_lobbies.text = "Error getting lobbies: $error"
                    }
            )
            queue.add(jsonObjectRequest)
        }
    }
}
