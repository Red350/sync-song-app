package red.padraig.syncsong.ui.adapater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import red.padraig.syncsong.R

// Adapter for displaying lobby information in a list view.
class ClientAdapter(
        context: Context,
        private val data: Array<String>,
        private val admin: String,
        private val userIsAdmin: Boolean,
        private val promoteClicked: (String) -> Unit
) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(i: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = convertView
        if (view == null) view = inflater.inflate(R.layout.row_client, parent, false)

        // At this stage the view will never be null, but this check allows kotlin to smart cast view to a non nullable View.
        if (view == null) return view

        val usernameView = view.findViewById<TextView>(R.id.rowclient_tv_username)
        val adminView = view.findViewById<ImageView>(R.id.rowclient_iv_admin)
        val promoteView = view.findViewById<Button>(R.id.rowclient_b_promote)

        val username = data[i]
        usernameView.text = username

        if (username == admin) {
            adminView.visibility = View.VISIBLE
        } else if (userIsAdmin) {
            promoteView.visibility = View.VISIBLE
            promoteView.setOnClickListener { promoteClicked(username) }
        }


        // Selecting these two views allows them to marquee scroll in situations where their text will not fit on screen.
        // This is done through a delayed call, so as to give the user time to read the start of the text before it scrolls.
        // Also due to how listview recycles its views, we need to un-select them at this stage or they may still be
        // selected from a previous time being displayed.
        usernameView.isSelected = false
        usernameView.postDelayed({ usernameView.isSelected = true }, 1000)

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