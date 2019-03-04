package red.padraig.syncsong

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsKtTest {
    @Test
    fun testEscapeSpecialCharactersBackslash() = assertEquals("""\\Tes\\t\\""", """\Tes\t\""".escapeSpecialCharacters())

    @Test
    fun testEscapeSpecialCharactersOpenCurlyBrace() = assertEquals("""\{Tes\{t\{""", """{Tes{t{""".escapeSpecialCharacters())

    @Test
    fun testEscapeSpecialCharactersCloseCurlyBrace() = assertEquals("""\}Tes\}t\}""", """}Tes}t}""".escapeSpecialCharacters())

    @Test
    fun testEscapeSpecialCharactersCombination() = assertEquals("""\\\{command: play \\\}""", """\{command: play \}""".escapeSpecialCharacters())

    @Test
    fun testUnescapeSpecialCharactersBackslash() = assertEquals("""\Tes\t\""", """\\Tes\\t\\""".unescapeSpecialCharacters())

    @Test
    fun testUnescapeSpecialCharactersOpenCurlyBrace() = assertEquals("""{Tes{t{""", """\{Tes\{t\{""".unescapeSpecialCharacters())

    @Test
    fun testUnescapeSpecialCharactersCloseCurlyBrace() = assertEquals("""}Tes}t}""", """\}Tes\}t\}""".unescapeSpecialCharacters())

    @Test
    fun testUnescapeSpecialCharactersCombination() = assertEquals("""\{command: play \}""", """\\\{command: play \\\}""".unescapeSpecialCharacters())

}