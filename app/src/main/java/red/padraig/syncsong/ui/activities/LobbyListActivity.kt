package red.padraig.syncsong.ui.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_lobby_list.*
import red.padraig.syncsong.R
import red.padraig.syncsong.ui.LobbyAdapter

class LobbyListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_list)

        lobbylist_lv_lobbies.adapter = LobbyAdapter(this, listOf("asdf", "test"))
    }
}
