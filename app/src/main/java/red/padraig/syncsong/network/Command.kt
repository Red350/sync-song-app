package red.padraig.syncsong.network

import kotlin.reflect.KClass

sealed class Command(val ordinal: Int)

sealed class ClientCommand(ordinal: Int) : Command(ordinal) {
    object AddSong : ClientCommand(0)
    object VoteSkip : ClientCommand(1)
}

sealed class ServerCommand(ordinal: Int) : Command(ordinal) {
    object Play : ServerCommand(0)
    object Pause : ServerCommand(1)
    object Resume : ServerCommand(2)
    object Skip : ServerCommand(3)
    object SeekTo : ServerCommand(4)
    object SeekRelative : ServerCommand(5)
    object Queue : ServerCommand(6)


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
fun getCommandByOrdinal(command: KClass<out Command>, ordinal: Int): Command {
    for (clazz in command.sealedSubclasses) {
        val obj = clazz.objectInstance
        if (obj?.ordinal == ordinal) {
            return obj
        }
    }
    throw ClassNotFoundException()
}
