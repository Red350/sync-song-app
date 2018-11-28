package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_lobbies.*

class LobbiesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobbies)

        val queue = Volley.newRequestQueue(this)
        val url = "http://padraig.red:8080/lobbies"

        Log.d("DEBUG", "creating request")
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d("DEBUG", response.toString())
                    lobbies_tv_response.text = response.toString()
                },
                Response.ErrorListener { error ->
                    println("Error getting lobbies: $error")
                }
        )

        queue.add(jsonArrayRequest)
    }
}
