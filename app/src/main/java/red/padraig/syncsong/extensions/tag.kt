package red.padraig.syncsong.extensions

import android.app.Activity

fun Activity.tag(): String = this::class.java.simpleName
