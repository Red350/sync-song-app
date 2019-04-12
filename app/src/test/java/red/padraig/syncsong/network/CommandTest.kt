package red.padraig.syncsong.network


import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import kotlin.reflect.KClass


class CommandTest {

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
    fun testServerCommand_HandshakeIsOne() {
        assertEquals(1, ServerCommand.Handshake.ordinal)
    }

    @Test
    fun testServerCommand_PlayIsTwo() {
        assertEquals(2, ServerCommand.Play.ordinal)
    }

    @Test
    fun testServerCommand_PauseIsThree() {
        assertEquals(3, ServerCommand.Pause.ordinal)
    }

    @Test
    fun testServerCommand_ResumeIsFour() {
        assertEquals(4, ServerCommand.Resume.ordinal)
    }

    @Test
    fun testServerCommand_SkipIsFive() {
        assertEquals(5, ServerCommand.Skip.ordinal)
    }

    @Test
    fun testServerCommand_SeekToIsSix() {
        assertEquals(6, ServerCommand.SeekTo.ordinal)
    }

    @Test
    fun testServerCommand_SeekRelativeIsSeven() {
        assertEquals(7, ServerCommand.SeekRelative.ordinal)
    }

    @Test
    fun testServerCommand_QueueIsEight() {
        assertEquals(8, ServerCommand.Queue.ordinal)
    }
}
