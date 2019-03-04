package red.padraig.syncsong

import android.app.Activity

fun Activity.tag(): String = this::class.java.simpleName

fun String.escapeSpecialCharacters(): String =
        this.replace(Regex("""\\"""), Regex.escapeReplacement("""\\"""))
                .replace(Regex("""\{"""), Regex.escapeReplacement("""\{"""))
                .replace(Regex("""\}"""), Regex.escapeReplacement("""\}"""))

fun String.unescapeSpecialCharacters(): String =
        this.replace(Regex("""\\\\"""), Regex.escapeReplacement("""\"""))
                .replace(Regex("""\\\{"""), Regex.escapeReplacement("""{"""))
                .replace(Regex("""\\\}"""), Regex.escapeReplacement("""}"""))
