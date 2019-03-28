package red.padraig.syncsong.network

import com.google.gson.annotations.SerializedName
import red.padraig.syncsong.data.MyTrack

data class Message(
        @SerializedName("username") val username: String?,
        @SerializedName("currentTrack") val track: MyTrack?,
        @SerializedName("command") val command: String?,
        @SerializedName("userMsg") val userMsg: String?
)
