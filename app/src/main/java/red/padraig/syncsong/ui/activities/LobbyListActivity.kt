package red.padraig.syncsong.ui.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import com.android.volley.NoConnectionError
import com.android.volley.Response
import com.android.volley.TimeoutError
import kotlinx.android.synthetic.main.activity_lobby_list.*
import red.padraig.syncsong.R
import red.padraig.syncsong.data.Lobby
import red.padraig.syncsong.printableError
import red.padraig.syncsong.tag
import red.padraig.syncsong.ui.adapater.LobbyAdapter


class LobbyListActivity : BaseActivity() {

    private val lobbyList = mutableListOf<Lobby>()
    private lateinit var lobbyAdapter: LobbyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_list)

        initListeners()

        initialiseActionBar("Lobbies")
    }

    private fun initListeners() {
        // Go to create lobby page on click.
        lobbylist_btn_createlobby.setOnClickListener { startActivity(Intent(this, CreateLobbyActivity::class.java)) }

        // Initialise lobby list view.
        lobbyAdapter = LobbyAdapter(this, lobbyList)
        lobbylist_lv_lobbies.adapter = lobbyAdapter
        // Join lobby on click row.
        lobbylist_lv_lobbies.setOnItemClickListener { _, _, i, _ ->
            if (lobbyList[i].public) {
                joinLobby(lobbyList[i].id, lobbyList[i].name)
                return@setOnItemClickListener
            }

            // Private lobby, prompt for lobby ID.
            displayJoinPrivateDialog(lobbyList[i].id, lobbyList[i].name)
        }

        // Initialise swipe to refresh.
        lobbylist_SRL_lobbies.setOnRefreshListener {
            this.getLobbies()
        }
    }

    override fun onResume() {
        super.onResume()

        this.getLobbies()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_lobbylist, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.lobbylist_menuitem_refresh -> {
            this.getLobbies()
            true
        }
        R.id.lobbylist_menuitem_joinbyid -> {
            displayJoinByIDDialog()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    // Request a list of lobbies from the server.
    // On response, this updates and notifies the lobbyAdapter.
    private fun getLobbies() {
        // Display the refreshing icon and remove the current lobby list.
        lobbylist_SRL_lobbies.isRefreshing = true
        lobbyList.clear()
        lobbyAdapter.notifyDataSetChanged()

        val errorListener = Response.ErrorListener { error ->
            val errorMsg = when (error) {
                is TimeoutError, is NoConnectionError -> {
                    "Unable to connect to sync song server"
                }
                else -> {
                    "Error getting lobbies: ${error.printableError()}"
                }
            }
            Log.d(this.tag(), errorMsg)
            toastLong(errorMsg)
            lobbylist_SRL_lobbies.isRefreshing = false
        }

        syncSongAPI.getLobbies(errorListener) {
            lobbyList.clear()   // No harm clearing again since this is asynchronous.
            lobbyList.addAll(it)
            lobbyAdapter.notifyDataSetChanged()

            // Hide the refreshing icon.
            lobbylist_SRL_lobbies.isRefreshing = false
        }
    }

    // Displays a dialog that allows a user to join a lobby by ID.
    private fun displayJoinByIDDialog() {
        val input = EditText(this)
        val builder = getDialogBuilder(input)

        builder.setPositiveButton("Join") { _, _ ->
            syncSongAPI.getLobby(
                    input.text.toString(),
                    Response.ErrorListener {
                        if (it.networkResponse.statusCode == 404) {
                            toastShort("Lobby does not exist")
                        } else {
                            toastShort("Error connecting to lobby: ${it.printableError()}")
                        }
                    }
            ) { lobby ->
                joinLobby(lobby.id, lobby.name)
            }
        }
        builder.show()
    }

    // Displays a dialog that compares the inputted ID against the lobbies ID, allowing the user
    // to join if they are the same.
    private fun displayJoinPrivateDialog(id: String, name: String) {
        val input = EditText(this)
        val builder = getDialogBuilder(input)

        builder.setPositiveButton("Join") { _, _ ->
            if (input.text.toString() == id) {
                joinLobby(id, name)
            } else {
                toastLong("Failed to join: Incorrect ID")
            }
        }
        builder.show()
    }

    private fun getDialogBuilder(input: EditText): AlertDialog.Builder {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Lobby ID")

        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
        builder.setView(input)

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        return builder
    }


    private fun joinLobby(id: String, title: String) {
        val intent = Intent(this, LobbyActivity::class.java)
        intent.putExtra("LOBBY_ID", id)
        intent.putExtra("LOBBY_NAME", title)
        startActivity(intent)
    }
}
