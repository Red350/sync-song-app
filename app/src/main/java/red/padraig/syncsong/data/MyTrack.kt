package red.padraig.syncsong.data

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.spotify.protocol.types.ImageUri

// I really don't like prefixing class names with "My", but the Spotify library has a Track class as
// well, and I'd rather avoid confusion.
data class MyTrack(
        @SerializedName("uri") val uri: String,
        @SerializedName("name") val name: String,
        @SerializedName("artist") val artist: String,
        // Current position of the track in millis. -1 indicates this field should be ignored.
        @SerializedName("position") val position: Long,
        // User who chose this track.
        @SerializedName("username") var username: String,
        @Transient val imageUri: ImageUri?,
        @Transient val artwork: Bitmap?
)
