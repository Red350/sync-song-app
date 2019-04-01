package red.padraig.syncsong.music

import android.graphics.Bitmap

interface MusicPlayer {

    fun connect()

    fun disconnect()

    fun play(uri: String)

    fun pause()

    fun resume()

    fun seekTo(uri: String, pos: Long)

    fun seekToRelativePosition(uri: String, pos: Long)

    fun skipNext()

    fun queue(uri: String)

    fun getCurrentImage(callback: (Bitmap?) -> Unit)
}
