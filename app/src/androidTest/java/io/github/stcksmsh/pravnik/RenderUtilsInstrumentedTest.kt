package io.github.stcksmsh.pravnik

import android.text.Spanned
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.stcksmsh.pravnik.ui.util.RenderUtils
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RenderUtilsInstrumentedTest {
    @Test
    fun clickable_refs_applied() {
        val src = "Vidi čl. 12 i član 34."
        val cs = RenderUtils.applyClickableArticleRefs(src) { _ ->
            object : android.text.style.ClickableSpan() { override fun onClick(widget: android.view.View) {} }
        } as Spanned
        val spans = cs.getSpans(0, src.length, android.text.style.ClickableSpan::class.java)
        assertTrue(spans.isNotEmpty())
    }
}
