package red.padraig.syncsong.network

import kotlin.reflect.KClass

sealed class Command(val ordinal: Int)

sealed class ClientCommand(ordinal: Int) : Command(ordinal) {
    object Handshake : ClientCommand(1)
    object AddSong : ClientCommand(2)
    object VoteSkip : ClientCommand(3)
    object Promote : ClientCommand(4)
}

sealed class ServerCommand(ordinal: Int) : Command(ordinal) {
    object Handshake: ServerCommand(1)
    object Play : ServerCommand(2)
    object Pause : ServerCommand(3)
    object Resume : ServerCommand(4)
    object Skip : ServerCommand(5)
    object SeekTo : ServerCommand(6)
    object SeekRelative : ServerCommand(7)
    object Queue : ServerCommand(8)
}

fun getServerCommandByOrdinal(ordinal: Int): ServerCommand {
    return getCommandByOrdinal(ServerCommand::class, ordinal) as ServerCommand
}

fun getClientCommandByOrdinal(ordinal: Int): ClientCommand {
    return getCommandByOrdinal(ClientCommand::class, ordinal) as ClientCommand
}

// Use reflection to get a command for a specific ordinal.
// Ideally this would be a static method in Command, but I can't find a way to use reflection to
// access the outer class from within a companion object.
@Throws(ClassNotFoundException::class)
fun getCommandByOrdinal(command: KClass<out Command>, ordinal: Int): Command {
    for (clazz in command.sealedSubclasses) {
        val obj = clazz.objectInstance
        if (obj?.ordinal == ordinal) {
            return obj
        }
    }
    throw ClassNotFoundException()
}
