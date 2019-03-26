package red.padraig.syncsong.ui.activities

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
            syncSongAPI.createLobby(
                    createlobby_et_name.text.toString(),
                    createlobby_spin_genre.selectedItem.toString(),
                    createlobby_rg_pub.isChecked,
                    Response.Listener { response ->
                        Log.d(this.tag(), "Create lobby response: $response")
                        Toast.makeText(this, response, Toast.LENGTH_SHORT).show()
                        onBackPressed()
                    }
            )
        }
    }

}
