package red.padraig.syncsong

import android.content.Context
import android.content.SharedPreferences
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Matchers.anyInt
import org.mockito.Matchers.anyString
import org.mockito.Mockito

class SharedPrefsWrapperTest {

    private lateinit var sharedPrefsWrapper: SharedPrefsWrapper
    private lateinit var mockSharedPrefs: SharedPreferences
    private lateinit var mockSharedPrefsEditor: SharedPreferences.Editor

    @Before
    fun setUp() {
        mockSharedPrefs = Mockito.mock(SharedPreferences::class.java)
        mockSharedPrefsEditor = Mockito.mock(SharedPreferences.Editor::class.java)
        val mockContext = Mockito.mock<Context>(Context::class.java)
        Mockito.`when`(mockContext.getSharedPreferences(anyString(), anyInt())).thenReturn(mockSharedPrefs)
        Mockito.`when`(mockSharedPrefs.edit()).thenReturn(mockSharedPrefsEditor)
        Mockito.`when`(mockSharedPrefsEditor.putString(anyString(), anyString())).thenReturn(mockSharedPrefsEditor)
        sharedPrefsWrapper = SharedPrefsWrapper(mockContext)
    }

    @After
    fun tearDown() {
    }

    @Test
    fun getToken() {
        Mockito.`when`(mockSharedPrefs.getString(SharedPrefsWrapper.TOKEN_KEY, "")).thenReturn("asdf")
        val token = sharedPrefsWrapper.token
        Assert.assertEquals("asdf", token)
    }

    @Test
    fun setToken() {
        sharedPrefsWrapper.token = "asdf"
        Mockito.verify(mockSharedPrefsEditor).putString(SharedPrefsWrapper.TOKEN_KEY, "asdf")
        Mockito.verify(mockSharedPrefsEditor).apply()
    }

    @Test
    fun getId() {
        Mockito.`when`(mockSharedPrefs.getString(SharedPrefsWrapper.ID_KEY, "")).thenReturn("123")
        val id = sharedPrefsWrapper.id
        Assert.assertEquals("123", id)
    }

    @Test
    fun setId() {
        sharedPrefsWrapper.id = "123"
        Mockito.verify(mockSharedPrefsEditor).putString(SharedPrefsWrapper.ID_KEY, "123")
        Mockito.verify(mockSharedPrefsEditor).apply()
    }

    @Test
    fun getUsername() {
        Mockito.`when`(mockSharedPrefs.getString(SharedPrefsWrapper.NAME_KEY, "")).thenReturn("blah")
        val username = sharedPrefsWrapper.username
        Assert.assertEquals("blah", username)
    }

    @Test
    fun setUsername() {
        sharedPrefsWrapper.username = "blah"
        Mockito.verify(mockSharedPrefsEditor).putString(SharedPrefsWrapper.NAME_KEY, "blah")
        Mockito.verify(mockSharedPrefsEditor).apply()
    }

    @Test
    fun getLobbyID() {
        Mockito.`when`(mockSharedPrefs.getString(SharedPrefsWrapper.LOBBYID_KEY, "")).thenReturn("ABCD")
        val lobbyID = sharedPrefsWrapper.lobbyID
        Assert.assertEquals("ABCD", lobbyID)
    }

    @Test
    fun setLobbyID() {
        sharedPrefsWrapper.lobbyID = "ABCD"
        Mockito.verify(mockSharedPrefsEditor).putString(SharedPrefsWrapper.LOBBYID_KEY, "ABCD")
        Mockito.verify(mockSharedPrefsEditor).apply()
    }

    @Test
    fun getSyncSongURL() {
        Mockito.`when`(mockSharedPrefs.getString(SharedPrefsWrapper.SYNC_SONG_URL_KEY, "")).thenReturn("http://url.com")
        val syncSongURL = sharedPrefsWrapper.syncSongURL
        Assert.assertEquals("http://url.com", syncSongURL)
    }

    @Test
    fun setSyncSongURL() {
        sharedPrefsWrapper.syncSongURL = "http://url.com"
        Mockito.verify(mockSharedPrefsEditor).putString(SharedPrefsWrapper.SYNC_SONG_URL_KEY, "http://url.com")
        Mockito.verify(mockSharedPrefsEditor).apply()
    }
}