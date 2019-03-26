package red.padraig.syncsong.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import red.padraig.syncsong.R
import red.padraig.syncsong.data.MyTrack

// Adapter for displaying track information in a list view.
class TrackAdapter(val context: Context, private val data: List<MyTrack>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_track, parent, false)

        // At this stage the view will never be null, but this check allows kotlin to smart cast view to a non nullable View.
        if (view == null) return view

        val trackNameView = view.findViewById<TextView>(R.id.rowtrack_tv_name)
        val artistNameView = view.findViewById<TextView>(R.id.rowtrack_tv_artist)
        val artworkView = view.findViewById<ImageView>(R.id.rowtrack_iv_artwork)

        // Load track info into the respective views.
        val track = data[i]
        trackNameView.text = track.name
        artistNameView.text = track.artist
        // If the artwork is not available, a default image will be displayed instead.
        if (track.artwork != null) {
            artworkView.setImageBitmap(track.artwork)
        } else {
            artworkView.setImageDrawable(context.getDrawable(R.drawable.ic_broken_image_black_64dp))
        }

        // Selecting these two views allows them to marquee scroll in situations where their text will not fit on screen.
        // This is done through a delayed call, so as to give the user time to read the start of the text before it scrolls.
        // Also due to how listview recycles its views, we need to un-select them at this stage or they may still be
        // selected from a previous time being displayed.
        artistNameView.isSelected = false
        trackNameView.isSelected = false
        artistNameView.postDelayed( { artistNameView.isSelected = true }, 1000)
        trackNameView.postDelayed( { trackNameView.isSelected = true }, 1000)

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