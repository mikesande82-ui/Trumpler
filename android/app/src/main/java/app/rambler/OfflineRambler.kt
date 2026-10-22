package app.rambler

import kotlin.random.Random

/**
 * The built-in rambler: no network and no model, just a phrase book. It keeps
 * the sender's message word for word, wraps it in tangents and circles back to
 * it, so whoever reads the ramble can still find what was meant.
 *
 * Plain Kotlin with no Android calls, so it runs in ordinary unit tests.
 */
object OfflineRambler {

    fun ramble(message: String, level: Level, random: Random = Random.Default): String {
        val text = message.trim().replace(WHITESPACE, " ")
        if (text.isEmpty()) return ""
        val topic = topicOf(text)
        val number = numberOf(text)
        val tangents = TANGENTS.shuffled(random).iterator()
        val riffs = TOPIC_RIFFS.shuffled(random).iterator()
        val backs = CIRCLE_BACKS.shuffled(random).iterator()

        // A riff on the message's own topic, or a tangent when it has none.
        fun riff(): String = if (topic != null) fill(riffs.next(), topic) else tangents.next()
        fun numberOrRiff(): String = if (number != null) numberRiff(number, random) else riff()

        val emphasis = random.pick(EMPHASIS)
        val parts = mutableListOf(random.pick(OPENERS), asSentence(text))
        when (level) {
            Level.LIGHT -> parts += numberOrRiff()
            Level.MEDIUM -> {
                parts += emphasis
                parts += numberOrRiff()
                parts += tangents.next()
                parts += backs.next()
                parts += recap(text)
            }
            Level.FULL -> {
                parts += emphasis
                if (number != null) parts += numberRiff(number, random)
                parts += riff()
                parts += tangents.next()
                parts += random.pick(SIR_STORIES)
                parts += tangents.next()
                parts += backs.next()
                parts += recap(text)
                parts += riff()
                parts += tangents.next()
                parts += backs.next()
                parts += recap(text)
                parts += "That's what I'm saying."
            }
        }
        parts += random.pick(CLOSERS.filter { it != emphasis })
        return parts.joinToString(" ")
    }

    /** The longest word worth riffing on, or null when the message has none. */
    internal fun topicOf(text: String): String? {
        var best: String? = null
        for (m in WORD.findAll(text)) {
            val w = m.value.removeSuffix("'s").removeSuffix("’s")
            if (w.length < 4 || w.lowercase() in STOPWORDS) continue
            if (best != null && w.length <= best.length) continue
            best = if (isName(w, text, m.range.first)) w else w.lowercase()
        }
        return best
    }

    /** The first plain whole number from 2 to 9999: not a time, price or decimal. */
    internal fun numberOf(text: String): Int? =
        NUMBER.find(text)?.value?.toInt()?.takeIf { it in 2..9999 }

    // Capitalised mid-sentence, or all capitals: a name or an acronym, so its
    // case stays. A capital at the start of a sentence says nothing.
    private fun isName(word: String, text: String, at: Int): Boolean {
        if (!word[0].isUpperCase()) return false
        if (word.length > 1 && word.all { it.isUpperCase() || !it.isLetter() }) return true
        val before = text.substring(0, at).trimEnd()
        return before.isNotEmpty() && before.last() !in ".!?"
    }

    private fun asSentence(s: String): String {
        val t = s.replaceFirstChar { it.titlecase() }
        return if (t.last().isLetterOrDigit()) "$t." else t
    }

    // The point again: the whole message when it is short, else its first sentence.
    private fun recap(text: String): String =
        asSentence(if (text.length <= 120) text else SENTENCE_END.split(text, 2)[0])

    private fun fill(template: String, topic: String): String =
        template.replace("{Topic}", topic.replaceFirstChar { it.titlecase() }).replace("{topic}", topic)

    private fun numberRiff(n: Int, random: Random): String =
        random.pick(NUMBER_RIFFS).replace("{n}", "$n").replace("{less}", "${n - 1}").replace("{more}", "${n + 1}")

    private fun <T> Random.pick(list: List<T>): T = list[nextInt(list.size)]

    private val WHITESPACE = Regex("\\s+")
    private val WORD = Regex("[A-Za-zÀ-ÿ][A-Za-zÀ-ÿ'’]*")
    private val NUMBER = Regex("(?<![\\d:.,$£€])\\b\\d{1,4}\\b(?![:.,]\\d)")
    private val SENTENCE_END = Regex("(?<=[.!?])\\s+")

    private val OPENERS = listOf(
        "Look, I'll say it.",
        "Let me tell you something, okay?",
        "So here's the thing, and nobody wants to say it, but I'll say it.",
        "Okay. Okay. Listen.",
        "I'm going to be honest with you, and people love that about me.",
        "A lot of people have been asking me about this. A lot of people.",
    )

    private val EMPHASIS = listOf(
        "And I mean that.",
        "That's the truth.",
        "Very simple.",
        "Everybody knows it.",
        "Nobody's ever said it better.",
    )

    private val TOPIC_RIFFS = listOf(
        "And {topic}, by the way. Nobody talks about {topic}. I talk about {topic}.",
        "And {topic}, let me tell you about {topic}. Some people say it's the best {topic} they've ever seen. Maybe ever. I don't know.",
        "{Topic}. Beautiful word. Some of the smartest people I know say it all the time.",
        "I've always been very good at {topic}, frankly. Better than anyone. Ask anybody.",
        "When it comes to {topic}, there's nobody better. Nobody. Not even close.",
    )

    private val NUMBER_RIFFS = listOf(
        "{n}. Some people say {less}, some people say {more}. It's {n}. It's always been {n}.",
        "And it's {n}, okay? Not {more}. A lot of people are saying {more}. It's {n}.",
        "{n}. Beautiful number. Some people would've said {less}. Not me. {n}.",
    )

    private val TANGENTS = listOf(
        "And the windmills. Don't get me started on the windmills.",
        "Have you seen what they charge for a hot dog now? Unbelievable. Terrible.",
        "My uncle, great guy, very smart, a professor, he told me about this a long time ago. Very smart family.",
        "And the sharks. I'm not a fan of the sharks. I don't think anybody is, frankly.",
        "The ceilings, by the way, were so high. Beautiful ceilings. You've never seen ceilings like it.",
        "And the weather, have you seen the weather? Nobody's ever seen weather like this.",
        "Traffic. Total disaster. Nobody fixed it, for years, nobody. I would've fixed it in a week.",
        "And the paper straws. They don't work. Everybody knows it, nobody says it.",
        "I was at a dinner the other night, a big dinner, beautiful dinner, and everyone was talking about it.",
        "And the batteries. You know what happens with the batteries? Nobody knows. I know.",
        "I have a very good brain, by the way. People say it all the time.",
        "They say I'm the best texter, maybe ever. I don't say it. They say it.",
    )

    private val SIR_STORIES = listOf(
        "A big, strong guy came up to me, tough guy, tears in his eyes, and he said, \"Sir, I've never seen anything like it.\" And I said, me neither.",
        "This guy comes up to me, rough guy, the toughest, crying, and he says, \"Sir, thank you.\" And I said, for what? He said, \"You know what for.\"",
        "A woman came up to me, very successful woman, and she said, \"Sir, how do you do it?\" And I said, I just do it. I just do it.",
    )

    private val CIRCLE_BACKS = listOf(
        "But getting back to what I was saying.",
        "Anyway, where was I? Oh, right.",
        "But that's not what we're talking about.",
        "So, very simply.",
    )

    private val CLOSERS = listOf(
        "Believe me.",
        "We'll see what happens.",
        "Everybody knows it.",
        "It's going to be tremendous.",
        "Thank you. Thank you very much.",
        "And that I can tell you.",
    )

    private val STOPWORDS = setOf(
        "about", "above", "across", "actually", "after", "again", "against", "almost", "along", "already",
        "also", "although", "always", "among", "another", "anyone", "anything", "around", "back", "because",
        "been", "before", "being", "below", "between", "both", "cannot", "can't", "could", "couldn't",
        "didn't", "does", "doesn't", "doing", "done", "don't", "down", "during", "each", "either", "else",
        "enough", "even", "ever", "every", "everyone", "everything", "from", "gets", "getting", "going",
        "gonna", "gotta", "guess", "hadn't", "haha", "hahaha", "hasn't", "have", "haven't", "having",
        "here", "here's", "hers", "herself", "himself", "into", "isn't", "it's", "itself", "just", "know",
        "let's", "like", "lmao", "maybe", "more", "most", "much", "must", "myself", "need", "never", "none",
        "nothing", "okay", "once", "only", "onto", "other", "ours", "ourselves", "over", "please", "pretty",
        "quite", "rather", "really", "said", "same", "says", "should", "shouldn't", "since", "some",
        "someone", "something", "still", "such", "sure", "than", "thank", "thanks", "that", "that's",
        "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd",
        "they'll", "they're", "they've", "thing", "things", "think", "this", "those", "though", "through",
        "till", "told", "toward", "under", "until", "upon", "very", "want", "wanna", "wasn't", "we'd",
        "we'll", "we're", "we've", "well", "were", "weren't", "what", "what's", "whatever", "when", "where",
        "whether", "which", "while", "who's", "whom", "whose", "will", "with", "within", "without", "won't",
        "would", "wouldn't", "yeah", "your", "you're", "you've", "you'll", "you'd", "yours", "yourself",
    )
}
