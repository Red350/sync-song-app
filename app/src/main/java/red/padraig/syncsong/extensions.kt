package red.padraig.syncsong

import android.app.Activity
import com.android.volley.VolleyError

fun Activity.tag(): String = this::class.java.simpleName

fun String.escapeSpecialCharacters(): String =
        this.replace(Regex("""\\"""), Regex.escapeReplacement("""\\"""))
                .replace(Regex("""\{"""), Regex.escapeReplacement("""\{"""))
                .replace(Regex("""\}"""), Regex.escapeReplacement("""\}"""))

fun String.unescapeSpecialCharacters(): String =
        this.replace(Regex("""\\\\"""), Regex.escapeReplacement("""\"""))
                .replace(Regex("""\\\{"""), Regex.escapeReplacement("""{"""))
                .replace(Regex("""\\\}"""), Regex.escapeReplacement("""}"""))

// Convenience function for printing volley errors.
fun VolleyError.printableError(): String = String(this.networkResponse.data)
