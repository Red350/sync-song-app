package red.padraig.syncsong.data

import android.graphics.Bitmap
import com.spotify.protocol.types.ImageUri

// I really don't like prefixing class names with "My", but the Spotify library has a Track class as
// well, and I'd rather avoid confusion.
data class MyTrack(val uri: String, val name: String, val artist: String, val imageUri: ImageUri?, val artwork: Bitmap?)