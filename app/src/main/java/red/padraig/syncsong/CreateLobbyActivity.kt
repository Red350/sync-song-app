package red.padraig.syncsong

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_create_lobby.*


class CreateLobbyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        createlobby_btn_create.setOnClickListener {
            val queue = Volley.newRequestQueue(this)
            val url = "http://padraig.red:8080/lobbies/create"

            Log.d("DEBUG", "creating lobby: $url")
            val createRequest = object : StringRequest(Request.Method.POST, url,
                    Response.Listener { response ->
                        Log.d("DEBUG", response.toString())
                        temp_create.text = response
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
