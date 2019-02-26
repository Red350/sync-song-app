package red.padraig.syncsong.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import red.padraig.syncsong.Lobby
import red.padraig.syncsong.R

class LobbyAdapter(context: Context, private val data: List<Lobby>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_lobby, parent, false)
        view?.findViewById<TextView>(R.id.rowlobby_tv_name)?.text = data[i].name
        view?.findViewById<TextView>(R.id.rowlobby_tv_genre)?.text = data[i].genre
        view?.findViewById<TextView>(R.id.rowlobby_tv_nummembers)?.text = data[i].numMembers.toString()
        view?.findViewById<TextView>(R.id.rowlobby_tv_pubpriv)?.text = data[i].public.toString()
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