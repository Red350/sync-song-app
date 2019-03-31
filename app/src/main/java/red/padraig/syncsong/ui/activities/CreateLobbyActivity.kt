package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_create_lobby.*
import red.padraig.syncsong.R
import red.padraig.syncsong.data.LobbyMode
import red.padraig.syncsong.tag


class CreateLobbyActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_lobby)

        initListeners()

        initialiseActionBar("Create Lobby")
    }

    private fun initListeners() {
        createlobby_btn_create.setOnClickListener {
            val name = createlobby_et_name.text.toString()
            val mode = LobbyMode.valueOf(findViewById<RadioButton>(createlobby_rg_lobbymode.checkedRadioButtonId).tag.toString())
            syncSongAPI.createLobby(
                    name,
                    mode,
                    createlobby_spin_genre.selectedItem.toString(),
                    createlobby_rg_pub.isChecked,
                    sharedPrefs.username,
                    Response.Listener { id ->
                        Log.d(this.tag(), "Create lobby response: $id")
                        toastShort("Created lobby with ID: $id")
                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.putExtra("LOBBY_ID", id)
                        intent.putExtra("LOBBY_NAME", name)
                        startActivity(intent)
                    }
            )
        }
    }

}
