package red.padraig.syncsong.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_search.*
import red.padraig.syncsong.R
import red.padraig.syncsong.tag
import java.util.*
import kotlin.collections.HashMap

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val queue = Volley.newRequestQueue(this)


        // TextWatcher that performs a spotify search after a specified delay since last character.
        // Debounce code taken from https://stackoverflow.com/a/54901542
        // TODO this could be extracted to an actual class for testing purposes.
        search_et_search.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            private val DELAY = 500L

            override fun afterTextChanged(s: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        Log.d(this@SearchActivity.tag(), "Sending search request: $s")
                        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, getString(R.string.spotify_url_search),
                                null,
                                Response.Listener { response ->
                                    Log.d(this@SearchActivity.tag(), "Search response: $response")
                                },
                                Response.ErrorListener { error ->
                                    Log.e(this@SearchActivity.tag(), "$error: ${error.networkResponse}")
                                    Toast.makeText(this@SearchActivity, "Error connecting to Spotify search: $error", Toast.LENGTH_LONG).show()
                                }
                        ) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Authorization"] = getString(R.string.CLIENT_ID)
                                return headers
                            }
                        }
                        queue.add(jsonObjectRequest)
                    }
                }, DELAY)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }
}
