package app.rambler

import android.app.Activity

/** One finished ramble, and how it was made. */
data class Ramble(val text: String, val note: String)

/**
 * The AI ramble when the server is set up and answers, else the offline one,
 * with a note saying which and why. The work runs on a background thread and
 * [done] runs back on the main thread, unless the screen has gone by then.
 */
fun Activity.ramble(settings: Settings, message: String, level: Level, done: (Ramble) -> Unit) {
    val useAi = settings.aiReady
    val url = settings.serverUrl
    val key = settings.appKey
    Thread {
        val result = if (!useAi) {
            Ramble(OfflineRambler.ramble(message, level), getString(R.string.note_offline))
        } else try {
            Ramble(RambleClient.ramble(url, key, message, level), getString(R.string.note_ai))
        } catch (e: Exception) {
            val why = (e as? RambleClient.Failure)?.message ?: getString(R.string.note_unreachable)
            Ramble(OfflineRambler.ramble(message, level), getString(R.string.note_fell_back, why))
        }
        runOnUiThread { if (!isFinishing && !isDestroyed) done(result) }
    }.start()
}
