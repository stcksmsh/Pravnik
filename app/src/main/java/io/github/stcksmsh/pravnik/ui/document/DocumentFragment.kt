package io.github.stcksmsh.pravnik.ui.document

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentDocumentBinding
import io.github.stcksmsh.pravnik.domain.repo.DefinitionsRepo
import io.github.stcksmsh.pravnik.domain.repo.TariffsRepo
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
    @Inject lateinit var definitionsRepo: DefinitionsRepo
    @Inject lateinit var tariffsRepo: TariffsRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        renderRead(args.docId, args.anchor)
    }

    private fun setupTabs() {
        val titles = listOf(
            getString(R.string.tab_read),
            getString(R.string.tab_definitions),
            getString(R.string.tab_tariffs),
            getString(R.string.tab_notes),
            getString(R.string.tab_meta),
            getString(R.string.tab_citations),
        )
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = titles.size
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ReadTab()
                    1 -> SimpleListTab()
                    2 -> SimpleListTab()
                    3 -> SimpleListTab()
                    4 -> SimpleListTab()
                    else -> SimpleListTab()
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
        binding.outlineBtn.setOnClickListener {
            // TODO: open outline drawer/sheet
        }
    }

    private fun renderRead(docId: String, anchor: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val unit = if (anchor.isNotEmpty()) unitsRepo.getByAnchor(docId, anchor) else null
            val title = unit?.title ?: "${'$'}docId#${'$'}anchor"
            binding.title.text = title
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
                            if (target != null) renderRead(docId, target.anchor) else Toast.makeText(requireContext(), "Target čl. ${'$'}number not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            // Place the read text into a simple TextView within a placeholder fragment container
            // For now, we reuse the fragment's own view by inflating a minimal layout
            val tv = TextView(requireContext())
            tv.movementMethod = LinkMovementMethod.getInstance()
            tv.text = withRefs
            // Replace the first page's content view using viewPager current fragment later; here we set title only
            binding.title.text = title
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

    class ReadTab : Fragment()
    class SimpleListTab : Fragment()
}
