package io.github.stcksmsh.pravnik

import android.text.Spanned
import io.github.stcksmsh.pravnik.ui.util.RenderUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RenderUtilsTest {
    @Test
    fun paragraphs_points_bullets_split() {
        val text = """
            Član 1
            (1) Prvi stav.
            – Alineja.
            1) Tačka jedna.

            (2) Drugi stav.
            2) Tačka dva.
        """.trimIndent()
        val blocks = RenderUtils.parseUnitText(text)
        assertEquals(2, blocks.size)
        assertTrue(blocks[0].body.contains("Prvi stav"))
        assertEquals(listOf("– Alineja.", "1) Tačka jedna."), blocks[0].points + blocks[0].bullets)
        assertTrue(blocks[1].body.contains("Drugi stav"))
    }

    @Test
    fun diacritic_insensitive_highlight_and_underline() {
        val src = "Ustav Crne Gore – član 12 sadrži odredbe."
        val highlighted = RenderUtils.highlightQuery(src, listOf("clan"), 0x6600FF00.toInt()) as Spanned
        // Ensure at least one span applied
        assertTrue(highlighted.getSpans(0, src.length, Any::class.java).isNotEmpty())

        val underlined = RenderUtils.underlineTerms(src, listOf("član")) as Spanned
        assertTrue(underlined.getSpans(0, src.length, Any::class.java).isNotEmpty())
    }

    @Test
    fun reference_detection_and_ranges() {
        val src = "Vidi čl. 12 i cl. 34, kao i član 56."
        val nums = RenderUtils.detectArticleRefs(src).sorted()
        assertEquals(listOf(12, 34, 56), nums)
        val ranges = RenderUtils.findArticleRefRanges(src)
        assertEquals(3, ranges.size)
        assertTrue(ranges.all { it.end > it.start && it.number > 0 })
    }
}
