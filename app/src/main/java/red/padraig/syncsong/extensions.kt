package red.padraig.syncsong

import android.app.Activity

fun Activity.tag(): String = this::class.java.simpleName

