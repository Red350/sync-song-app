package red.padraig.syncsong.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_create_lobby.*
import red.padraig.syncsong.R
import red.padraig.syncsong.tag


class CreateLobbyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        createlobby_btn_create.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://padraig.red:8080/lobbies/create"

            Log.d(this.tag(), "creating lobby: $url")
            val createRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        Log.d(this.tag(), response.toString())
                        Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    },
                    Response.ErrorListener { error ->
                        println("Error creating lobby: $error")
                    }
            ) {
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params["id"] = createlobby_et_id.text.toString()
                    params["name"] = createlobby_et_name.text.toString()

                    return params
                }
            }

            queue.add(createRequest)
        }
    }
}
