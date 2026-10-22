package app.rambler

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

/** Try a ramble, and set up the AI server. The real use is the selection menu (ProcessTextActivity). */
class MainActivity : Activity() {

    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = Settings(this)

        val input = findViewById<EditText>(R.id.input)
        val go = findViewById<Button>(R.id.go)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val output = findViewById<TextView>(R.id.output)
        val note = findViewById<TextView>(R.id.note)
        val copy = findViewById<Button>(R.id.copy)

        findViewById<RadioGroup>(R.id.level).bindLevel(settings.level) { settings.level = it }

        go.setOnClickListener {
            val message = input.text.toString()
            if (message.isBlank()) {
                input.error = getString(R.string.type_something)
                return@setOnClickListener
            }
            go.isEnabled = false
            progress.visibility = View.VISIBLE
            ramble(settings, message, settings.level) { r ->
                go.isEnabled = true
                progress.visibility = View.GONE
                output.text = r.text
                note.text = r.note
                output.visibility = View.VISIBLE
                note.visibility = View.VISIBLE
                copy.visibility = View.VISIBLE
            }
        }
        copy.setOnClickListener { copyToClipboard(output.text.toString()) }

        val server = findViewById<EditText>(R.id.server_url)
        val key = findViewById<EditText>(R.id.app_key)
        val useAi = findViewById<CheckBox>(R.id.use_ai)
        server.setText(settings.serverUrl)
        key.setText(settings.appKey)
        useAi.isChecked = settings.useAi
        findViewById<Button>(R.id.save).setOnClickListener {
            val url = server.text.toString().trim()
            if (url.isNotEmpty() && !url.startsWith("https://")) {
                server.error = getString(R.string.needs_https)
                return@setOnClickListener
            }
            settings.serverUrl = url
            settings.appKey = key.text.toString()
            settings.useAi = useAi.isChecked
            Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        }
    }
}
