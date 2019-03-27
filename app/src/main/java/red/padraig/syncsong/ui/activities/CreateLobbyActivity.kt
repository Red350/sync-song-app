package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_create_lobby.*
import red.padraig.syncsong.R
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
            syncSongAPI.createLobby(
                    name,
                    createlobby_spin_genre.selectedItem.toString(),
                    createlobby_rg_pub.isChecked,
                    sharedPrefs.username,
                    Response.Listener { id ->
                        Log.d(this.tag(), "Create lobby response: $id")
                        Toast.makeText(this, "Created lobby with ID: $id", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LobbyActivity::class.java)
                        intent.putExtra("LOBBY_ID", id)
                        intent.putExtra("LOBBY_NAME", name)
                        startActivity(intent)
                    }
            )
        }
    }

}
