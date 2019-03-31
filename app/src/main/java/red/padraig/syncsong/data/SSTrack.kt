package red.padraig.syncsong.data

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.spotify.protocol.types.ImageUri

// Sync Song Track. An object representing a single track within this app.
data class SSTrack(
        @SerializedName("uri") val uri: String,
        @SerializedName("name") val name: String,
        @SerializedName("artist") val artist: String,
        // Duration of the song in millis.
        @SerializedName("duration") val duration: Long,
        // Current position of the track in millis. -1 indicates this field should be ignored.
        @SerializedName("position") val position: Long = -1L,
        // User who chose this track.
        @SerializedName("username") var username: String = "",
        @Transient val imageUri: ImageUri? = null,
        @Transient val artwork: Bitmap? = null
) : Parcelable {
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    constructor(parcel: Parcel) : this(
            uri = parcel.readString(),
            name = parcel.readString(),
            artist = parcel.readString(),
            duration = parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(uri)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeLong(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SSTrack> {
        override fun createFromParcel(parcel: Parcel): SSTrack {
            return SSTrack(parcel)
        }

        override fun newArray(size: Int): Array<SSTrack?> {
            return arrayOfNulls(size)
        }
    }
}
