package red.padraig.syncsong.ui.activities

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.android.volley.RequestQueue
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.common_toolbar.*
import red.padraig.syncsong.SharedPrefsWrapper
import red.padraig.syncsong.SyncSongApplication
import red.padraig.syncsong.network.SyncSongAPI

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var sharedPrefs: SharedPrefsWrapper
    protected lateinit var volleyQueue: RequestQueue
    protected lateinit var syncSongAPI: SyncSongAPI

    // Required to inject the context for custom font loading.
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = (application as SyncSongApplication).sharedPrefsWrapper
        volleyQueue = (application as SyncSongApplication).volleyQueue
        syncSongAPI = (application as SyncSongApplication).syncSongAPI
    }

    // Set a custom font for a text view, using a font from the assets/fonts directory.
    fun setFont(tv: TextView, fontName: String) {
        tv.typeface = Typeface.createFromAsset(assets, "fonts/$fontName")
    }

    fun hideKeyboard() {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }

    fun initialiseActionBar(title: String) {
        setSupportActionBar(common_toolbar)
        supportActionBar?.title = title
    }
}
