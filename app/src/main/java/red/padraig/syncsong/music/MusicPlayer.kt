package red.padraig.syncsong.music

import android.graphics.Bitmap

interface MusicPlayer {

    fun connect()

    fun disconnect()

    fun play(uri: String)

    fun pause()

    fun resume()

    fun seekTo(pos: Long)

    fun seekToRelativePosition(pos: Long)

    fun skipNext()

    fun queue(uri: String)

    fun getCurrentImage(callback: (Bitmap?) -> Unit)
}
