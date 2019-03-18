package red.padraig.syncsong.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Track

class TrackAdapter(context: Context, private val data: List<Track>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_track, parent, false)
        view?.findViewById<TextView>(R.id.rowtrack_tv_name)?.text = data[i].name
        view?.findViewById<TextView>(R.id.rowtrack_tv_artist)?.text = data[i].artist
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