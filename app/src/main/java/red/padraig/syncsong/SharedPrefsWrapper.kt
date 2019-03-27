package red.padraig.syncsong

import android.content.Context
import android.content.SharedPreferences

// Convenience class for accessing values stored in shared preferences as though they were properties.
@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SharedPrefsWrapper(context: Context) {

    companion object {
        const val SHARED_PREFS_TAG = "syncSongSharedPrefs"
        const val TOKEN_KEY = "token"
        const val ID_KEY = "id"
        const val NAME_KEY = "name"
        const val LOBBYID_KEY = "lobbyID"
    }

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(SHARED_PREFS_TAG, Context.MODE_PRIVATE)

    var token: String
        get() = sharedPreferences.getString(TOKEN_KEY, "")
        set(value) = sharedPreferences.edit().putString(TOKEN_KEY, value).apply()

    var id: String
        get() = sharedPreferences.getString(ID_KEY, "")
        set(value) = sharedPreferences.edit().putString(ID_KEY, value).apply()

    var username: String
        get() = sharedPreferences.getString(NAME_KEY, "")
        set(value) = sharedPreferences.edit().putString(NAME_KEY, value).apply()

    // Stores the ID of the currently connected lobby.
    // Used to determine if a user is already in the current lobby on starting the activity.
    var lobbyID: String
        get() = sharedPreferences.getString(LOBBYID_KEY, "")
        set(value) = sharedPreferences.edit().putString(LOBBYID_KEY, value).apply()
}
