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
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentDocumentBinding
import io.github.stcksmsh.pravnik.domain.repo.DefinitionsRepo
import io.github.stcksmsh.pravnik.domain.repo.TariffsRepo
import io.github.stcksmsh.pravnik.domain.repo.UnitsRepo
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import io.github.stcksmsh.pravnik.domain.repo.HistoryRepo
import io.github.stcksmsh.pravnik.domain.model.History
import io.github.stcksmsh.pravnik.ui.document.tabs.ReadTabFragment
import io.github.stcksmsh.pravnik.ui.document.tabs.SimpleListTabFragment
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
    @Inject lateinit var documentsRepo: DocumentsRepo
    @Inject lateinit var historyRepo: HistoryRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDocumentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.starBtn.setOnClickListener { toggleStar() }

        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        // Update title from document
        viewLifecycleOwner.lifecycleScope.launch {
            val doc = documentsRepo.get(args.docId)
            binding.title.text = doc?.title ?: args.docId
        }
        // Insert into history
        viewLifecycleOwner.lifecycleScope.launch {
            historyRepo.insert(History(id = 0, docId = args.docId, unitAnchor = args.anchor ?: "", visitedAt = System.currentTimeMillis()))
        }
        parentFragmentManager.setFragmentResultListener("doc_anchor", viewLifecycleOwner) { _, bundle ->
            val anchor = bundle.getString("anchor").orEmpty()
            if (anchor.isNotEmpty()) {
                val action = DocumentFragmentDirections.actionDocumentSelf(args.docId, anchor)
                findNavController().navigate(action)
            }
        }
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
        val docId = args.docId
        val anchor = args.anchor
        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = titles.size
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> ReadTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("anchor", anchor) } }
                    1 -> SimpleListTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("mode", "defs") } }
                    2 -> SimpleListTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("mode", "tariffs") } }
                    3 -> SimpleListTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("mode", "notes") } }
                    4 -> SimpleListTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("mode", "meta") } }
                    else -> SimpleListTabFragment().apply { arguments = Bundle().apply { putString("docId", docId); putString("mode", "citations") } }
                }
            }
        }
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = titles[pos]
        }.attach()
        binding.outlineBtn.setOnClickListener { openOutline() }
    }

    private fun openOutline() {
        val ctx = requireContext()
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(ctx)
        val v = layoutInflater.inflate(R.layout.bottom_sheet_outline, null)
        val list = v.findViewById<android.widget.ListView>(R.id.outlineList)
        viewLifecycleOwner.lifecycleScope.launch {
            val labels = unitsRepo.listLabels(args.docId)

            list.adapter = android.widget.ArrayAdapter(ctx, android.R.layout.simple_list_item_1, labels.map { it.label })
            list.setOnItemClickListener { _, _, position, _ ->
                val anchor = labels[position].anchor
                parentFragmentManager.setFragmentResult("doc_anchor", Bundle().apply { putString("anchor", anchor) })
                dialog.dismiss()
            }
        }
        dialog.setContentView(v)
        dialog.show()
    }

    private fun toggleStar() {
        viewLifecycleOwner.lifecycleScope.launch {
            val docId = args.docId
            val starred = documentsRepo.listStarred().any { it.id == docId }
            if (starred) documentsRepo.unstar(docId) else documentsRepo.star(docId)
            Toast.makeText(requireContext(), if (starred) R.string.unstarred else R.string.starred, Toast.LENGTH_SHORT).show()
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
