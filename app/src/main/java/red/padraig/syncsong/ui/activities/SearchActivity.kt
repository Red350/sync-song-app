package red.padraig.syncsong.ui.activities

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_search.*
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Track
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.TrackAdapter
import java.util.*
import kotlin.collections.HashMap

class SearchActivity : BaseActivity() {

    private val trackList = mutableListOf<Track>()
    private lateinit var trackAdapter: TrackAdapter

    val baseUrl: Uri.Builder = Uri.Builder()
            .scheme("https")
            .authority("api.spotify.com")
            .appendPath("v1")
            .appendPath("search")
            .appendQueryParameter("type", "track")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        trackAdapter = TrackAdapter(this, trackList)
        search_lv_tracks.adapter = trackAdapter
        search_lv_tracks.setOnItemClickListener { _, _, i, _ ->
            Log.d(this.tag(), trackList[i].toString())
        }

        // TextWatcher that performs a spotify search after a specified delay since last character.
        // Debounce code taken from https://stackoverflow.com/a/54901542
        // TODO this could be extracted to an actual class for testing purposes.
        search_et_search.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            private val DELAY = 500L

            override fun afterTextChanged(searchQuery: Editable?) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        val searchUrl = baseUrl.appendQueryParameter("q", searchQuery.toString()).build()
                        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET,
                                searchUrl.toString(), null,
                                Response.Listener { response ->
                                    Log.d(this@SearchActivity.tag(), "Search response: $response")
                                    trackList.clear()
                                    val parser = JsonParser()
                                    val tracks = parser.parse(response.toString()).asJsonObject["tracks"].asJsonObject["items"].asJsonArray
                                    System.out.println(tracks.toString())
                                    tracks.forEach {
                                        trackList.add(Track(
                                                it.asJsonObject["uri"].asString,
                                                it.asJsonObject["name"].asString,
                                                // Convert the artist object into a comma separated list of names.
                                                it.asJsonObject["artists"].asJsonArray.joinToString { artist ->
                                                    artist.asJsonObject["name"].asString
                                                }
                                        ))
                                    }
                                    trackAdapter.notifyDataSetChanged()
                                },
                                Response.ErrorListener { error ->
                                    Log.e(this@SearchActivity.tag(), "$error: ${error.networkResponse}")
                                    Toast.makeText(this@SearchActivity, "Error connecting to Spotify search: $error", Toast.LENGTH_LONG).show()
                                }
                        ) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Authorization"] = "Bearer ${sharedPrefs.token}"
                                return headers
                            }
                        }
                        Log.d(this@SearchActivity.tag(), "Sending search request: $searchQuery")
                        volleyQueue.add(jsonObjectRequest)
                    }
                }, DELAY)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }
}
