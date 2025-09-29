package io.github.stcksmsh.pravnik.ui.util

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.UnderlineSpan
import android.text.style.ClickableSpan
import androidx.annotation.ColorInt
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

object RenderUtils {
    // Paragraphs: split on "\n((\d+))\s" keeping markers with their paragraph
    private val paragraphSplitRegex = Regex("""\n\((\n?\d+)\)\s""")

    // Points: lines like "1) ..." (allow leading spaces)
    private val pointLineRegex = Regex("""^\s*\d+\)""")

    // Bullets: lines starting with en dash "– " (allow leading spaces)
    private val bulletLineRegex = Regex("""^\s*–\s""")

    // Intra-doc refs: čl. 12, cl. 12, član 12, clan 12
    private val articleRefPattern: Pattern = Pattern.compile(
        """(?iu)(?:\bčl(?:\.|an)?|\bcl(?:\.|an)?)\s*(\n?\d+)\b"""
    )

    data class ParagraphBlock(
        val body: String,
        val points: List<String> = emptyList(),
        val bullets: List<String> = emptyList(),
    )

    fun splitParagraphs(text: String): List<String> {
        if (text.isEmpty()) return emptyList()
        val parts = mutableListOf<String>()
        var last = 0
        val m = Regex("""\n\((\d+)\)\s""").toPattern().matcher(text)
        while (m.find()) {
            val idx = m.start()
            if (idx > last) parts += text.substring(last, idx)
            last = idx + 1 // drop the leading \n on the next chunk
        }
        parts += text.substring(last)
        return parts.filter { it.isNotBlank() }
    }

    fun parseUnitText(text: String): List<ParagraphBlock> {
        val paras = splitParagraphs(text)
        return paras.map { p ->
            val lines = p.lines()
            val points = mutableListOf<String>()
            val bullets = mutableListOf<String>()
            val bodyLines = mutableListOf<String>()
            for (line in lines) {
                when {
                    pointLineRegex.containsMatchIn(line) -> points += line.trim()
                    bulletLineRegex.containsMatchIn(line) -> bullets += line.trim()
                    else -> bodyLines += line
                }
            }
            ParagraphBlock(
                body = bodyLines.joinToString("\n").trim(),
                points = points,
                bullets = bullets,
            )
        }
    }

    fun extractPoints(lines: List<String>): List<String> =
        lines.filter { pointLineRegex.containsMatchIn(it) }

    fun extractBullets(lines: List<String>): List<String> =
        lines.filter { bulletLineRegex.containsMatchIn(it) }

    // --- Diacritic-insensitive helpers ---
    private fun normalizeAndMap(source: String): Pair<String, IntArray> {
        val normBuilder = StringBuilder()
        val indexMap = ArrayList<Int>()
        for (i in source.indices) {
            val ch = source[i].toString()
            val nfd = Normalizer.normalize(ch, Normalizer.Form.NFD)
            val stripped = nfd.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            for (c in stripped) {
                normBuilder.append(c)
                indexMap.add(i)
            }
        }
        return normBuilder.toString().lowercase(Locale.ROOT) to indexMap.toIntArray()
    }

    fun normalizeForSearch(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase(Locale.ROOT)

    fun tokenizeQuery(q: String): List<String> =
        q.split("\n", "\t", " ", ",", ".", ";", ":", "(", ")", "[", "]", "\u2013", "\u2014")
            .map { it.trim() }
            .filter { it.length >= 2 }

    // Highlight tokens diacritic-insensitively
    fun highlightQuery(text: String, tokens: List<String>, @ColorInt color: Int): CharSequence {
        if (text.isEmpty() || tokens.isEmpty()) return text
        val (norm, map) = normalizeAndMap(text)
        val sb = SpannableStringBuilder(text)
        for (raw in tokens) {
            val q = normalizeForSearch(raw)
            var from = 0
            while (from <= norm.length - q.length) {
                val idx = norm.indexOf(q, from)
                if (idx < 0) break
                val start = map[idx]
                val end = map[idx + q.length - 1] + 1
                sb.setSpan(BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                from = idx + q.length
            }
        }
        return sb
    }

    // Underline defined terms diacritic-insensitively
    fun underlineTerms(text: String, terms: List<String>): CharSequence {
        if (text.isEmpty() || terms.isEmpty()) return text
        val (norm, map) = normalizeAndMap(text)
        val sb = SpannableStringBuilder(text)
        for (t in terms) {
            val q = normalizeForSearch(t)
            var from = 0
            while (from <= norm.length - q.length) {
                val idx = norm.indexOf(q, from)
                if (idx < 0) break
                val start = map[idx]
                val end = map[idx + q.length - 1] + 1
                sb.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                from = idx + q.length
            }
        }
        return sb
    }

    // Detect references like čl. 12, returns the captured number as Int
    fun detectArticleRefs(text: String): List<Int> {
        val out = mutableListOf<Int>()
        val m = articleRefPattern.matcher(text)
        while (m.find()) {
            m.group(1)?.replace("\n", "")?.toIntOrNull()?.let { out += it }
        }
        return out
    }

    data class RefRange(val start: Int, val end: Int, val number: Int)

    fun findArticleRefRanges(text: String): List<RefRange> {
        val res = mutableListOf<RefRange>()
        val m = articleRefPattern.matcher(text)
        while (m.find()) {
            val raw = m.group(1)?.replace("\n", "")
            val n = raw?.toIntOrNull() ?: continue
            res += RefRange(m.start(), m.end(), n)
        }
        return res
    }

    fun applyClickableArticleRefs(text: String, spanFor: (Int) -> ClickableSpan): CharSequence {
        val sb = SpannableStringBuilder(text)
        val m = articleRefPattern.matcher(text)
        while (m.find()) {
            val raw = m.group(1)?.replace("\n", "")
            val n = raw?.toIntOrNull() ?: continue
            sb.setSpan(spanFor(n), m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        return sb
    }
}
