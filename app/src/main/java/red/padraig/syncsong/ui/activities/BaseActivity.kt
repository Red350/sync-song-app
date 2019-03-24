package red.padraig.syncsong.ui.activities

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.android.volley.RequestQueue
import red.padraig.syncsong.SharedPrefsWrapper
import red.padraig.syncsong.SyncSongApplication

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var sharedPrefs: SharedPrefsWrapper
    protected lateinit var volleyQueue: RequestQueue

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
