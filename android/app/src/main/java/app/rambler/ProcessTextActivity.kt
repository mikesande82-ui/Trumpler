package app.rambler

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView

/**
 * The "Ramble" item in any app's text-selection menu. Android hands over the
 * selected text; if it came from a field the user can edit, Replace hands the
 * ramble back and the host app swaps it in. Otherwise Copy is the way out.
 */
class ProcessTextActivity : Activity() {

    private lateinit var settings: Settings
    private lateinit var output: TextView
    private lateinit var note: TextView
    private lateinit var progress: ProgressBar
    private lateinit var buttons: List<Button>

    private var original = ""
    private var latest: String? = null
    private var ticket = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_text)
        settings = Settings(this)
        output = findViewById(R.id.output)
        note = findViewById(R.id.note)
        progress = findViewById(R.id.progress)
        output.movementMethod = ScrollingMovementMethod()

        original = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString().orEmpty()
        if (original.isBlank()) {
            finish()
            return
        }
        val readOnly = intent.getBooleanExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, false)

        val again = findViewById<Button>(R.id.again)
        val copy = findViewById<Button>(R.id.copy)
        val replace = findViewById<Button>(R.id.replace)
        buttons = listOf(again, copy, replace)
        replace.visibility = if (readOnly) View.GONE else View.VISIBLE

        again.setOnClickListener { run() }
        copy.setOnClickListener { latest?.let { copyToClipboard(it) } }
        replace.setOnClickListener {
            latest?.let {
                setResult(RESULT_OK, Intent().putExtra(Intent.EXTRA_PROCESS_TEXT, it))
                finish()
            }
        }
        findViewById<RadioGroup>(R.id.level).bindLevel(settings.level) {
            settings.level = it
            run()
        }

        val kept = savedInstanceState?.getString(LATEST)
        if (kept != null) show(Ramble(kept, savedInstanceState.getString(NOTE).orEmpty()))
        else run()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        latest?.let {
            outState.putString(LATEST, it)
            outState.putString(NOTE, note.text.toString())
        }
    }

    private fun run() {
        val mine = ++ticket
        progress.visibility = View.VISIBLE
        buttons.forEach { it.isEnabled = false }
        ramble(settings, original, settings.level) { if (mine == ticket) show(it) }
    }

    private fun show(r: Ramble) {
        latest = r.text
        output.text = r.text
        output.scrollTo(0, 0)
        note.text = r.note
        progress.visibility = View.GONE
        buttons.forEach { it.isEnabled = true }
    }

    private companion object {
        const val LATEST = "latest"
        const val NOTE = "note"
    }
}
