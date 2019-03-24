package red.padraig.syncsong.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
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

    val baseUrl = Uri.Builder()
            .scheme("https")
            .authority("api.spotify.com")
            .appendPath("v1")
            .appendPath("search")
            .appendQueryParameter("type", "track")
            .build()
            .toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        trackAdapter = TrackAdapter(this, trackList)
        search_lv_tracks.adapter = trackAdapter
        search_lv_tracks.setOnItemClickListener { _, _, i, _ ->
            // Return the selected track to the Lobby activity.
            val returnIntent = Intent()
            returnIntent.putExtra("track", trackList[i])
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }

        // Set a scroll listener to hide the keyboard when the user scrolls the list view.
        search_lv_tracks.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScroll(view: AbsListView?, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
                // Pass.
            }

            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            }

        })

        // TextWatcher that performs a spotify search after a specified delay since last character.
        // Debounce code taken from https://stackoverflow.com/a/54901542
        // TODO this could be extracted to an actual class for testing purposes.
        search_et_search.addTextChangedListener(object : TextWatcher {
            private var timer = Timer()
            private val DELAY = 500L

            override fun afterTextChanged(searchQuery: Editable?) {
                // The timer should be cancelled even when an empty search is entered.
                timer.cancel()
                if (searchQuery.toString() == "") return
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        val searchUrl = "$baseUrl&q=$searchQuery"
                        val tempTrackList = mutableListOf<Track>()
                        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET,
                                searchUrl, null,
                                Response.Listener { response ->
                                    Log.d(this@SearchActivity.tag(), "Search response: $response")
                                    tempTrackList.clear()   // Probably not necessary, but left over from before I used a temp list.
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
                                        // We should still add a track without album art.
                                        if (images.size() == 0) {
                                            addTrack(tempTrackList, Track(uri, name, artists, null))
                                            return@forEach  // Don't send an image request.
                                        }
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
                                                    addTrack(tempTrackList, Track(uri, name, artists, artwork))
                                                },
                                                0,
                                                0,
                                                ImageView.ScaleType.CENTER_CROP,
                                                Bitmap.Config.RGB_565,
                                                Response.ErrorListener { error ->
                                                    Log.e(this@SearchActivity.tag(), "Failed to get album art: ${error.printableError()}")
                                                    // We should still display a track even if the album art doesn't load.
                                                    addTrack(tempTrackList, Track(uri, name, artists, null))
                                                }
                                        ))
                                    }
                                },
                                Response.ErrorListener { error ->
                                    Log.e(this@SearchActivity.tag(), "Spotify search request failed: ${error.printableError()}")
                                    Toast.makeText(this@SearchActivity, "Error connecting to Spotify search: $error", Toast.LENGTH_LONG).show()
                                }
                        ) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val headers = HashMap<String, String>()
                                headers["Authorization"] = "Bearer ${sharedPrefs.token}"
                                return headers
                            }
                        }
                        Log.d(this@SearchActivity.tag(), "Sending search request for \"$searchQuery\" ($searchUrl)")
                        volleyQueue.add(jsonObjectRequest)
                    }
                }, DELAY)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // TODO strip newline characters and perform search immediately when user hits enter.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }


    // TODO look into whether calling this multiple times can cause issues. Seems fine currently.
    // An alternate solution would be to keep track of the number of requests sent vs responses received, and trigger after all responses received.
    // Volley might also have some support for batch requests such as this.
    private fun addTrack(tempTrackList: MutableList<Track>, track: Track) {
        tempTrackList.add(track)
        // This isn't the neatest solution, but previously when I was editing the trackList directly,
        // there was a race condition that could result in the adapter trying to access outside of the array.
        // It's possible this could be solved by calling notifyDataSetChanged() after clearing the array,
        // but this solution works and doesn't seem to affect performance.
        trackList.clear()
        trackList.addAll(tempTrackList)
        trackAdapter.notifyDataSetChanged()
    }
}
