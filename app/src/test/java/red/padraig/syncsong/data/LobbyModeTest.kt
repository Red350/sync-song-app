package red.padraig.syncsong.data

import org.junit.Assert.assertEquals
import org.junit.Test

class LobbyModeTest {

    @Test
    fun testGetModeString_AdminControlled() = assertEquals(LobbyMode.ADMIN_CONTROLLED, LobbyMode.getLobbyModeByModeString("Admin-controlled"))

    @Test
    fun testGetModeString_RoundRobin() = assertEquals(LobbyMode.ROUND_ROBIN, LobbyMode.getLobbyModeByModeString("Round-robin"))

    @Test
    fun testGetModeString_FreeForAll() = assertEquals(LobbyMode.FREE_FOR_ALL, LobbyMode.getLobbyModeByModeString("Free-for-all"))

    @Test(expected = NoSuchElementException::class)
    fun testGetModeString_InvalidString() {
        LobbyMode.getLobbyModeByModeString("This mode doesn't exist")
    }

}