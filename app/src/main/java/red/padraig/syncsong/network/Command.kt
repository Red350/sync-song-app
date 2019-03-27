package red.padraig.syncsong.network

sealed class Command(val ordinal: Int)

sealed class ClientCommand(ordinal: Int): Command(ordinal) {
    object AddSong : ClientCommand(0)
    object VoteSkip: ClientCommand(1)
}

sealed class ServerCommand(code: Int): Command(code) {
    object Play: ServerCommand(0)
    object Pause: ServerCommand(1)
    object Resume: ServerCommand(2)
    object Skip: ServerCommand(3)
    object SeekTo: ServerCommand(4)
    object SeekRelative: ServerCommand(5)
    object Queue: ServerCommand(6)
}
