package io.github.stcksmsh.pravnik.ui.document

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentDocumentBinding
import io.github.stcksmsh.pravnik.domain.repo.UnitsRepo
import io.github.stcksmsh.pravnik.ui.util.RenderUtils
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DocumentFragment : Fragment() {
    private var _binding: FragmentDocumentBinding? = null
    private val binding get() = _binding!!

    private val args: DocumentFragmentArgs by navArgs()

    @Inject lateinit var unitsRepo: UnitsRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textContent.movementMethod = LinkMovementMethod.getInstance()
        render(args.docId, args.anchor)
    }

    private fun render(docId: String, anchor: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val unit = if (anchor.isNotEmpty()) unitsRepo.getByAnchor(docId, anchor) else null
            val title = unit?.title ?: "${'$'}docId#${'$'}anchor"
            val bodyText = unit?.text ?: sampleText()

            binding.title.text = title

            val blocks = RenderUtils.parseUnitText(bodyText)
            val sb = StringBuilder()
            blocks.forEachIndexed { idx, b ->
                if (b.body.isNotBlank()) sb.append(b.body.trim()).append('\n')
                if (b.points.isNotEmpty()) {
                    b.points.forEach { sb.append(it).append('\n') }
                }
                if (b.bullets.isNotEmpty()) {
                    b.bullets.forEach { sb.append(it).append('\n') }
                }
                if (idx < blocks.lastIndex) sb.append('\n')
            }

            // Underline definitions if available (placeholder terms)
            val underlined = RenderUtils.underlineTerms(sb.toString(), listOf("pojam", "definicija"))
            // Highlight current anchor or query if provided via arguments (optional)
            val highlighted = RenderUtils.highlightQuery(underlined.toString(), RenderUtils.tokenizeQuery(args.anchor), 0x443498DB.toInt())

            val withRefs = RenderUtils.applyClickableArticleRefs(highlighted.toString()) { number ->
                object : android.text.style.ClickableSpan() {
                    override fun onClick(widget: View) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val target = unitsRepo.getByNumber(docId, number)
                            if (target != null) {
                                render(docId, target.anchor)
                            } else {
                                Toast.makeText(requireContext(), "Target čl. ${'$'}number not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            binding.textContent.text = withRefs
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
