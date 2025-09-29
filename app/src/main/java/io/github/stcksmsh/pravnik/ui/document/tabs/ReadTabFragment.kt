package io.github.stcksmsh.pravnik.ui.document.tabs

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.domain.repo.UnitsRepo
import io.github.stcksmsh.pravnik.ui.util.RenderUtils
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReadTabFragment : Fragment() {
    @Inject lateinit var unitsRepo: UnitsRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(io.github.stcksmsh.pravnik.R.layout.fragment_read_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val docId = requireArguments().getString("docId")!!
        val anchor = requireArguments().getString("anchor").orEmpty()
        val tv: TextView = view.findViewById(io.github.stcksmsh.pravnik.R.id.readText)
        tv.movementMethod = LinkMovementMethod.getInstance()
        viewLifecycleOwner.lifecycleScope.launch {
            val unit = if (anchor.isNotEmpty()) unitsRepo.getByAnchor(docId, anchor) else null
            val bodyText = unit?.text ?: sampleText()
            val blocks = RenderUtils.parseUnitText(bodyText)
            val sb = StringBuilder()
            blocks.forEachIndexed { idx, b ->
                if (b.body.isNotBlank()) sb.append(b.body.trim()).append('\n')
                if (b.points.isNotEmpty()) b.points.forEach { sb.append(it).append('\n') }
                if (b.bullets.isNotEmpty()) b.bullets.forEach { sb.append(it).append('\n') }
                if (idx < blocks.lastIndex) sb.append('\n')
            }
            val underlined = RenderUtils.underlineTerms(sb.toString(), listOf("pojam", "definicija"))
            val highlighted = RenderUtils.highlightQuery(underlined.toString(), RenderUtils.tokenizeQuery(anchor), 0x443498DB.toInt())
            val withRefs = RenderUtils.applyClickableArticleRefs(highlighted.toString()) { number ->
                object : android.text.style.ClickableSpan() {
                    override fun onClick(widget: View) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val target = unitsRepo.getByNumber(docId, number)
                            if (target != null) setAnchor(target.anchor) else Toast.makeText(requireContext(), "Target čl. $number not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            tv.text = withRefs
        }
    }

    private fun setAnchor(anchor: String) {
        parentFragmentManager.setFragmentResult(
            "doc_anchor",
            Bundle().apply { putString("anchor", anchor) }
        )
    }

    private fun sampleText(): String = """
        Član 1
        (1) Ovo je prvi stav ovog člana propisa.
        – Prva alineja sa primjerom.
        1) Prva tačka primera.
        2) Druga tačka primera.
        Vidi čl. 2 za nastavak.

        Član 2
        (1) Nastavak teksta i upućivanje na cl. 1.
    """.trimIndent()
}
