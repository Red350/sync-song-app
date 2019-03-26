package red.padraig.syncsong.network

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import red.padraig.syncsong.R
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag

class SyncSongAPI(private val context: Context, private val volleyQueue: RequestQueue) {

    fun createLobby(name: String, genre: String, public: Boolean, responseListener: Response.Listener<String>) {
        val url = context.getString(R.string.api_url_1) + context.getString(R.string.api_port) + context.getString(R.string.api_endpoint_createlobby)

        Log.d(this.tag(), "Sending create lobby request: $url")
        val createRequest = object : StringRequest(Request.Method.POST, url,
                responseListener,
                Response.ErrorListener { error ->
                    val errorMsg = "Error creating lobby: ${error.printableError()}"
                    Log.d(this.tag(), errorMsg)
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