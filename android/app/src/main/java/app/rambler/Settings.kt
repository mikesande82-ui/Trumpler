package app.rambler

import android.content.Context

/**
 * What the user set on the main screen. The server address and app key start
 * from whatever the build baked in (RAMBLE_SERVER_URL and RAMBLE_APP_KEY in
 * the build's environment), so a build made with them works with no typing.
 */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("rambler", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString(SERVER_URL, null) ?: BuildConfig.DEFAULT_SERVER_URL
        set(v) = prefs.edit().putString(SERVER_URL, v.trim()).apply()

    var appKey: String
        get() = prefs.getString(APP_KEY, null) ?: BuildConfig.DEFAULT_APP_KEY
        set(v) = prefs.edit().putString(APP_KEY, v.trim()).apply()

    var useAi: Boolean
        get() = prefs.getBoolean(USE_AI, true)
        set(v) = prefs.edit().putBoolean(USE_AI, v).apply()

    var level: Level
        get() = Level.fromWire(prefs.getString(LEVEL, null))
        set(v) = prefs.edit().putString(LEVEL, v.wire).apply()

    val aiReady: Boolean
        get() = useAi && serverUrl.isNotBlank() && appKey.isNotBlank()

    private companion object {
        const val SERVER_URL = "serverUrl"
        const val APP_KEY = "appKey"
        const val USE_AI = "useAi"
        const val LEVEL = "level"
    }
}
