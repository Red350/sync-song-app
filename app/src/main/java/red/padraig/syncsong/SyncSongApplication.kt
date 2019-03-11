package red.padraig.syncsong

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

class SyncSongApplication: Application() {

    lateinit var sharedPrefsWrapper: SharedPrefsWrapper
    lateinit var volleyQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()
        sharedPrefsWrapper = SharedPrefsWrapper(applicationContext)
        volleyQueue = Volley.newRequestQueue(applicationContext)
    }
}
