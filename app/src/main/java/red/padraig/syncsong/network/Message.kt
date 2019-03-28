package red.padraig.syncsong.network

import com.google.gson.annotations.SerializedName
import red.padraig.syncsong.data.MyTrack

data class Message(
        @SerializedName("username") var username: String?,
        @SerializedName("currentTrack") val track: MyTrack?,
        @SerializedName("trackQueue") val trackQueue: Array<MyTrack>?,
        @SerializedName("command") val command: Int?,
        @SerializedName("userMsg") val userMsg: String?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (username != other.username) return false
        if (track != other.track) return false
        if (trackQueue != null) {
            if (other.trackQueue == null) return false
            if (!trackQueue.contentEquals(other.trackQueue)) return false
        } else if (other.trackQueue != null) return false
        if (command != other.command) return false
        if (userMsg != other.userMsg) return false

        return true
    }

    override fun hashCode(): Int {
        var result = username?.hashCode() ?: 0
        result = 31 * result + (track?.hashCode() ?: 0)
        result = 31 * result + (trackQueue?.contentHashCode() ?: 0)
        result = 31 * result + (command ?: 0)
        result = 31 * result + (userMsg?.hashCode() ?: 0)
        return result
    }
}
