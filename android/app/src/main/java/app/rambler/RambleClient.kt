package app.rambler

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject

/** Asks the Rambler server (api/ramble.js) for an AI ramble. Blocking: call it off the main thread. */
object RambleClient {

    class Failure(message: String) : IOException(message)

    fun ramble(serverUrl: String, appKey: String, message: String, level: Level): String {
        val conn = URL(serverUrl).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 10_000
            conn.readTimeout = 30_000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.setRequestProperty("X-App-Key", appKey)
            val body = JSONObject().put("text", message).put("level", level.wire).toString()
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val reply = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (code != 200) throw Failure(
                when (code) {
                    401 -> "the app key doesn't match the server's"
                    413 -> "the message is too long for the AI"
                    422 -> "the AI passed on this one"
                    429 -> "too many rambles, slow down"
                    else -> "the server answered $code"
                }
            )
            return JSONObject(reply).optString("ramble").ifBlank { throw Failure("the server sent back nothing") }
        } finally {
            conn.disconnect()
        }
    }
}
