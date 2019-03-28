package red.padraig.syncsong.data

enum class LobbyMode(val modeString: String) {

    // Mode strings allows a lobby mode to be determined by string.
    // Ideally these strings would use a string resource to ensure they always stay lined up with
    // parts of the app that call getLobbyModeByModeString. Unfortunately that requires a context
    // instance, which isn't really possible to get in an Enum without doing something hacky like
    // providing a static instance in the Application class which is highly frowned upon.
    UNUSABLE("do not use"), // Go can not distinguish between 0 and null, so all enums must start at 1.
    ADMIN_CONTROLLED("Admin-controlled"),
    FREE_FOR_ALL("Free-for-all"),
    ROUND_ROBIN("Round-robin");

    companion object {
        fun getLobbyModeByModeString(name: String): LobbyMode {
            return values().first { it.modeString == name }
        }
    }
}
