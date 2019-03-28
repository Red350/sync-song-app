package red.padraig.syncsong.ui.adapater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import red.padraig.syncsong.R
import red.padraig.syncsong.data.MyTrack

// Adapter for displaying track information in a list view.
class QueueAdapter(val context: Context, private val data: List<MyTrack>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_queue, parent, false)

        // At this stage the view will never be null, but this check allows kotlin to smart cast view to a non nullable View.
        if (view == null) return view

        val songArtistView = view.findViewById<TextView>(R.id.rowqueue_tv_songartist)
        val userView = view.findViewById<TextView>(R.id.rowqueue_tv_addedby)

        // Load track info into the respective views.
        val track = data[i]
        songArtistView.text = "${track.name} - ${track.artist}"
        userView.text = track.username

        // Selecting these two views allows them to marquee scroll in situations where their text will not fit on screen.
        // This is done through a delayed call, so as to give the user time to read the start of the text before it scrolls.
        // Also due to how listview recycles its views, we need to un-select them at this stage or they may still be
        // selected from a previous time being displayed.
        songArtistView.isSelected = false
        userView.isSelected = false
        songArtistView.postDelayed( { songArtistView.isSelected = true }, 1000)
        userView.postDelayed( { userView.isSelected = true }, 1000)

        return view
    }

    override fun getItem(i: Int): Any {
        return data[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    override fun getCount(): Int {
        return data.size
    }

}