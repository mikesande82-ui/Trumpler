package app.rambler

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.RadioGroup
import android.widget.Toast

/** Both screens' Light / Medium / Full weave buttons. */
fun RadioGroup.bindLevel(initial: Level, onChange: (Level) -> Unit) {
    check(
        when (initial) {
            Level.LIGHT -> R.id.level_light
            Level.MEDIUM -> R.id.level_medium
            Level.FULL -> R.id.level_full
        }
    )
    setOnCheckedChangeListener { _, id ->
        onChange(
            when (id) {
                R.id.level_light -> Level.LIGHT
                R.id.level_full -> Level.FULL
                else -> Level.MEDIUM
            }
        )
    }
}

fun Context.copyToClipboard(text: String) {
    getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), text))
    // Android 13 and later show their own "Copied" bubble.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) Toast.makeText(this, R.string.copied, Toast.LENGTH_SHORT).show()
}
