package app.rambler

/** How far the ramble wanders. [wire] is the name the server knows it by. */
enum class Level(val wire: String) {
    LIGHT("light"),
    MEDIUM("medium"),
    FULL("full");

    companion object {
        fun fromWire(s: String?): Level = entries.firstOrNull { it.wire == s } ?: MEDIUM
    }
}
