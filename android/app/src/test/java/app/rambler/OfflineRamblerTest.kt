package app.rambler

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineRamblerTest {

    private val messages = listOf(
        "Running late, be there in 10.",
        "Can you grab milk on the way home?",
        "ok",
        "Dinner at Luigi's tonight at 7:30? Bring Sarah.",
        "Meeting moved to Thursday",
        "I can't make it tonight, sorry. Rain check? Maybe next week works better for everyone, " +
            "because the kids have practice and I still have to finish the report before Friday.",
    )

    private fun all(): List<Triple<String, Level, String>> =
        messages.flatMap { m -> Level.entries.flatMap { l -> (0 until 25).map { s -> Triple(m, l, OfflineRambler.ramble(m, l, Random(s))) } } }

    @Test
    fun keepsTheMessageWordForWord() {
        for ((message, level, out) in all()) {
            val core = message.trimEnd('.', '?', '!').drop(1)
            assertTrue("$level lost \"$message\": $out", out.contains(core))
        }
    }

    @Test
    fun leavesNoPlaceholdersBehind() {
        for ((_, _, out) in all()) assertTrue(out, '{' !in out && '}' !in out)
    }

    @Test
    fun wandersFurtherAtHigherLevels() {
        fun words(level: Level) = messages.sumOf { m -> (0 until 25).sumOf { s -> OfflineRambler.ramble(m, level, Random(s)).split(' ').size } }
        val light = words(Level.LIGHT)
        val medium = words(Level.MEDIUM)
        val full = words(Level.FULL)
        assertTrue("light $light, medium $medium, full $full", light < medium && medium < full)
    }

    @Test
    fun sameSeedSameRamble() {
        assertEquals(
            OfflineRambler.ramble("Running late", Level.FULL, Random(7)),
            OfflineRambler.ramble("Running late", Level.FULL, Random(7)),
        )
    }

    @Test
    fun nothingInNothingOut() {
        assertEquals("", OfflineRambler.ramble("  \n ", Level.FULL))
    }

    @Test
    fun haggles() {
        for (s in 0 until 25) {
            val out = OfflineRambler.ramble("be there in 10", Level.FULL, Random(s))
            assertTrue(out, Regex("\\b(9|11)\\b").containsMatchIn(out))
        }
    }

    @Test
    fun picksTheBiggestWordAndKeepsNamesCapitalised() {
        assertEquals("running", OfflineRambler.topicOf("Running late, be there in 10."))
        assertEquals("Thursday", OfflineRambler.topicOf("Meeting moved to Thursday"))
        assertEquals("tonight", OfflineRambler.topicOf("Dinner at Luigi's tonight?"))
        assertEquals("Luigi", OfflineRambler.topicOf("Meet at Luigi's"))
        assertNull(OfflineRambler.topicOf("ok, see you"))
    }

    @Test
    fun onlyPlainNumbersGetHaggledOver() {
        assertEquals(10, OfflineRambler.numberOf("be there in 10"))
        assertNull(OfflineRambler.numberOf("dinner at 7:30"))
        assertNull(OfflineRambler.numberOf("it was $20"))
        assertNull(OfflineRambler.numberOf("about 2.5 miles"))
        assertNull(OfflineRambler.numberOf("just 1"))
    }
}
