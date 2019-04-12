package red.padraig.syncsong.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Lobby

// Adapter for displaying lobby information in a list view.
class LobbyAdapter(context: Context, private val data: List<Lobby>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_lobby, parent, false)

        // At this stage the view will never be null, but this check allows kotlin to smart cast view to a non nullable View.
        if (view == null) return view

        // Load lobby data into respective views.
        val lobby = data[i]
        val lobbyNameView = view.findViewById<TextView>(R.id.rowlobby_tv_name)
        lobbyNameView.text = lobby.name
        view.findViewById<TextView>(R.id.rowlobby_tv_genre).text = lobby.genre
        view.findViewById<TextView>(R.id.rowlobby_tv_nummembers).text = lobby.numMembers.toString()
        // Private lobbies display a lock icon.
        view.findViewById<ImageView>(R.id.rowlobby_iv_private)?.visibility = if (lobby.public) View.INVISIBLE else View.VISIBLE

        // Selecting this view allows it to marquee scroll in situations where the text will not fit on screen.
        // This is done through a delayed call, so as to give the user time to read the start of the text before it scrolls.
        lobbyNameView.postDelayed( { lobbyNameView.isSelected = true }, 1000)
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