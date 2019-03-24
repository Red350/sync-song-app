package red.padraig.syncsong

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump


class SyncSongApplication : Application() {

    lateinit var sharedPrefsWrapper: SharedPrefsWrapper
    lateinit var volleyQueue: RequestQueue

    override fun onCreate() {
        super.onCreate()
        sharedPrefsWrapper = SharedPrefsWrapper(applicationContext)
        volleyQueue = Volley.newRequestQueue(applicationContext)

        // Initialise custom font loader.
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Montserrat-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build())
    }

}
