package red.padraig.syncsong.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Track

class TrackAdapter(val context: Context, private val data: List<Track>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_track, parent, false)
        val track = data[i]
        view?.findViewById<TextView>(R.id.rowtrack_tv_name)?.text = track.name
        view?.findViewById<TextView>(R.id.rowtrack_tv_artist)?.text = track.artist
        if (track.artwork != null) {
            view?.findViewById<ImageView>(R.id.rowtrack_iv_artwork)?.setImageBitmap(track.artwork)
        } else {
            view?.findViewById<ImageView>(R.id.rowtrack_iv_artwork)?.setImageDrawable(context.getDrawable(R.drawable.ic_broken_image_black_64dp))
        }
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