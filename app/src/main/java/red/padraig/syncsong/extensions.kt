package red.padraig.syncsong

import com.android.volley.VolleyError

fun Any.tag(): String = this::class.java.simpleName

fun String.escapeSpecialCharacters(): String =
        this.replace(Regex("""\\"""), Regex.escapeReplacement("""\\"""))
                .replace(Regex("""\{"""), Regex.escapeReplacement("""\{"""))
                .replace(Regex("""\}"""), Regex.escapeReplacement("""\}"""))

fun String.unescapeSpecialCharacters(): String =
        this.replace(Regex("""\\\\"""), Regex.escapeReplacement("""\"""))
                .replace(Regex("""\\\{"""), Regex.escapeReplacement("""{"""))
                .replace(Regex("""\\\}"""), Regex.escapeReplacement("""}"""))

// Convenience function for printing volley errors.
fun VolleyError.printableError(): String {
    return if (this.networkResponse != null) {
        String(this.networkResponse.data)
    } else {
        this::class.simpleName.toString()
    }
}

// Print the ordinal value of a number e.g. 1st, 2nd etc.
// Courtesy of https://stackoverflow.com/a/6810409/11184227
fun Int.ordinal(): String {
    val suffixes = arrayOf("th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th")
    return when (this % 100) {
        11, 12, 13 -> this.toString() + "th"
        else -> this.toString() + suffixes[this % 10]
    }
}
