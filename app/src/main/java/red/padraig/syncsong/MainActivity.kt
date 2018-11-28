package red.padraig.syncsong

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_btn_viewlobbies.setOnClickListener { startActivity(Intent(this, LobbiesActivity::class.java)) }
        main_btn_createlobby.setOnClickListener { startActivity(Intent(this, CreateLobbyActivity::class.java)) }
        main_btn_spotify.setOnClickListener { startActivity(Intent(this, SpotifyActivity::class.java)) }

        main_btn_joinlobby.setOnClickListener{
            val queue = Volley.newRequestQueue(this)
            val url = "http://padraig.red:8080/lobbies/" + main_et_joinlobby.text + "/join"

            Log.d("DEBUG", "joining lobby: $url")
            val joinRequest = JsonObjectRequest(Request.Method.GET, url, null,
                    Response.Listener { response ->
                        Log.d("DEBUG", response.toString())
                        temp.text = response.toString()
                    },
                    Response.ErrorListener { error ->
                        println("Error joining lobby: $error")
                    }
            )

            queue.add(joinRequest)
        }
    }

}
