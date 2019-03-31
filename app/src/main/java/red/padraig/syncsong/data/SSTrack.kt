package red.padraig.syncsong.data

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.spotify.protocol.types.ImageUri

// Sync Song Track. An object representing a single track within this app.
data class SSTrack(
        @SerializedName("uri") val uri: String,
        @SerializedName("name") val name: String,
        @SerializedName("artist") val artist: String,
        // Current position of the track in millis. -1 indicates this field should be ignored.
        @SerializedName("position") val position: Long = -1L,
        // User who chose this track.
        @SerializedName("username") var username: String = "",
        @Transient val imageUri: ImageUri? = null,
        @Transient val artwork: Bitmap? = null
)
