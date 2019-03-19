package red.padraig.syncsong.ui.activities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_search.*
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Track
import red.padraig.syncsong.printableError
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

                                    // Parse the json for each track in the response. A request is also made for each track for the album artwork.
                                    tracks.forEach {
                                        val track = it.asJsonObject
                                        val uri = track["uri"].asString
                                        val name = track["name"].asString
                                        // Convert the artist object into a comma separated list of names.
                                        val artists = track["artists"].asJsonArray.joinToString { artist ->
                                            artist.asJsonObject["name"].asString
                                        }

                                        // Album art URIs are stored in an array in descending order of size, we're looking for the smallest.
                                        val images = it.asJsonObject["album"].asJsonObject["images"].asJsonArray
                                        // Images are stored in descending order of size, we're looking for the second largest (300x300 px).
                                        // If for whatever reason there is only a single image in the array, we just take that instead.
                                        val imageIndex = when(images.size()) {
                                            1 -> 0
                                            else -> 1
                                        }
                                        val imageUrl = images[imageIndex].asJsonObject["url"].asString
                                        // TODO we should be caching these images, and checking at this point if a query is required.
                                        Log.d(this@SearchActivity.tag(), "Sending request for artwork: $imageUrl")
                                        volleyQueue.add(ImageRequest(
                                                imageUrl,
                                                Response.Listener<Bitmap> { artwork ->
                                                    Log.d(this@SearchActivity.tag(), "Received artwork response: $imageUrl")
                                                    trackList.add(Track(uri, name, artists, artwork))
                                                    // TODO look into whether calling this multiple times can cause issues. Seems fine currently.
                                                    // An alternate solution would be to keep track of the number of requests sent vs responses received, and trigger after all responses received.
                                                    // Volley might also have some support for batch requests such as this.
                                                    trackAdapter.notifyDataSetChanged()
                                                },
                                                0,
                                                0,
                                                ImageView.ScaleType.CENTER_CROP,
                                                Bitmap.Config.RGB_565,
                                                Response.ErrorListener { error ->
                                                    Log.e(this@SearchActivity.tag(), "Failed to get album art: ${error.printableError()}")
                                                    // We should still display a track even if the album art doesn't load.
                                                    trackList.add(Track(uri, name, artists, null))
                                                }
                                        ))
                                    }
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
