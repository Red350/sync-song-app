package red.padraig.syncsong.network

import junit.framework.Assert.assertEquals
import junit.framework.Assert.fail
import org.junit.Test
import kotlin.reflect.KClass


class CommandTest {

    @Test
    fun testClientCommand_HandshakeIsOne() {
        assertEquals(1, ClientCommand.Handshake.ordinal)
    }

    @Test
    fun testClientCommand_AddSongIsTwo() {
        assertEquals(2, ClientCommand.AddSong.ordinal)
    }

    @Test
    fun testClientCommand_VoteSkipIsThree() {
        assertEquals(3, ClientCommand.VoteSkip.ordinal)
    }

    @Test
    fun testClientCommand_PromoteIsFour() {
        assertEquals(4, ClientCommand.Promote.ordinal)
    }

    @Test
    fun testClientCommand_OrdinalsUnique() {
        checkSealedSubclassOrdinalsUnique(ClientCommand::class)
    }

    @Test
    fun testServerCommand_OrdinalsUnique() {
        checkSealedSubclassOrdinalsUnique(ServerCommand::class)
    }

    private fun checkSealedSubclassOrdinalsUnique(command: KClass<out Command>) {
        val classes = command.sealedSubclasses

        val commands = mutableListOf<Command>()
        for (clazz in classes) {
            val obj = clazz.objectInstance
            if (obj != null) {
                commands.add(obj)
            }
        }

        // Make sure this test is actually checking the ordinals.
        if (commands.isEmpty()) {
            fail()
        }

        val counts = commands.groupingBy{ it.ordinal }.eachCount()
        counts.forEach { _, count -> assertEquals(1, count) }
    }
}
