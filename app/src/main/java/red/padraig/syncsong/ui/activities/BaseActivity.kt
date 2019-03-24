package red.padraig.syncsong.ui.activities

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.android.volley.RequestQueue
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import red.padraig.syncsong.SharedPrefsWrapper
import red.padraig.syncsong.SyncSongApplication

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var sharedPrefs: SharedPrefsWrapper
    protected lateinit var volleyQueue: RequestQueue

    // Required to inject the context for custom font loading.
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPrefs = (application as SyncSongApplication).sharedPrefsWrapper
        volleyQueue = (application as SyncSongApplication).volleyQueue
    }

    // Set a custom font for a text view, using a font from the assets/fonts directory.
    fun setFont(tv: TextView, fontName: String) {
        tv.typeface = Typeface.createFromAsset(assets, "fonts/$fontName")
    }
}
