package red.padraig.syncsong

import android.content.Context
import android.content.SharedPreferences

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SharedPrefsWrapper(context: Context) {

    companion object {
        const val SHARED_PREFS_TAG = "syncSongSharedPrefs"
        const val TOKEN_KEY = "token"
        const val ID_KEY = "id"
        const val NAME_KEY = "name"
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
}
