package red.padraig.syncsong.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_client_list.*
import red.padraig.syncsong.R
import red.padraig.syncsong.ui.adapter.ClientAdapter

class ClientListActivity : BaseActivity() {

    private lateinit var clientList: Array<String>
    private lateinit var admin: String
    private lateinit var clientAdapter: ClientAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_list)

        admin = intent.getStringExtra("ADMIN")
        clientList = intent.getStringArrayExtra("CLIENTS")
        initListeners()

        initialiseActionBar("Lobby Members")
    }

    private fun initListeners() {
        clientAdapter = ClientAdapter(this, clientList, admin, sharedPrefs.username == admin, this::promoteClicked)
        clientlist_lv_clients.adapter = clientAdapter
    }

    // Return the selected client to the calling activity.
    private fun promoteClicked(username: String) {
        val returnIntent = Intent()
        returnIntent.putExtra("ADMIN", username)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
