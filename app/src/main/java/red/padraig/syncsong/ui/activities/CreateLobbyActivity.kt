package red.padraig.syncsong.ui.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import kotlinx.android.synthetic.main.activity_create_lobby.*
import red.padraig.syncsong.R
import red.padraig.syncsong.tag


class CreateLobbyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        initialiseActionBar("Create Lobby")

        createlobby_btn_create.setOnClickListener {
            val url = getString(R.string.api_url_1) + getString(R.string.api_port) + getString(R.string.api_endpoint_createlobby)

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
                    params["name"] = createlobby_et_name.text.toString()
                    params["genre"] = createlobby_spin_genre.selectedItem.toString()
                    params["public"] = createlobby_rg_pub.isChecked.toString()

                    return params
                }
            }

            volleyQueue.add(createRequest)
        }
    }

}
