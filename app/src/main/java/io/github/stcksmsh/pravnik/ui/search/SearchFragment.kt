package io.github.stcksmsh.pravnik.ui.search

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentSearchBinding
import io.github.stcksmsh.pravnik.domain.model.DocumentType
import androidx.navigation.fragment.findNavController
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import io.github.stcksmsh.pravnik.domain.repo.SearchRepo
import io.github.stcksmsh.pravnik.domain.repo.SearchFilters
import io.github.stcksmsh.pravnik.ui.util.RenderUtils
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var searchRepo: SearchRepo
    @Inject lateinit var documentsRepo: DocumentsRepo

    private val adapter = ResultsAdapter { row ->
        // Navigate to document view on click
        val action = SearchFragmentDirections.actionSearchToDocument(row.docId, row.anchor)
        findNavController().navigate(action)
    }
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resultsList.adapter = adapter
        setupTypeChips()
        binding.queryInput.addTextChangedListener { text ->
            scheduleSearch(text?.toString())
        }
        val initial = arguments?.getString("initialQuery").orEmpty()
        if (initial.isNotBlank()) {
            binding.queryInput.setText(initial)
            scheduleSearch(initial)
        }
    }

    private fun scheduleSearch(raw: String?) {
        job?.cancel()
        job = viewLifecycleOwner.lifecycleScope.launch {
            delay(250)
            val q = raw?.trim().orEmpty()
            if (q.isBlank()) { adapter.submitList(emptyList()); return@launch }
            val filters = collectFilters()
            val res = searchRepo.search(q, filters)
            adapter.submitList(res.rows.map { row -> UiRow(row.docId, row.unitAnchor, row.docTitle, row.unitTitle, row.snippetHtml, q) })
            renderFacets(res, filters)
        }
    }

    private fun setupTypeChips() {
        val types = listOf(DocumentType.LAW, DocumentType.CONSTITUTION, DocumentType.BYLAW, DocumentType.CASE, DocumentType.PRACTICE)
        val cg = binding.typeChips
        cg.removeAllViews()
        types.forEach { t ->
            val chip = layoutInflater.inflate(R.layout.view_choice_chip, cg, false) as Chip
            chip.text = t.name
            chip.isChecked = true
            chip.setOnCheckedChangeListener { _, _ -> scheduleSearch(binding.queryInput.text?.toString()) }
            cg.addView(chip)
        }
    }

    private fun collectFilters(): SearchFilters {
        val selectedTypes = binding.typeChips.children.mapNotNull { (it as? Chip)?.takeIf { c -> c.isChecked }?.text?.toString()?.let { DocumentType.valueOf(it) } }.toSet()
        val selectedDocIds = binding.docChips.children.mapNotNull { (it as? Chip)?.takeIf { c -> c.isChecked }?.tag?.toString() }.toSet()
        val court = (binding.courtChips.children.firstOrNull { (it as? Chip)?.isChecked == true } as? Chip)?.tag?.toString()
        val caseYear = (binding.caseYearChips.children.firstOrNull { (it as? Chip)?.isChecked == true } as? Chip)?.tag?.toString()?.toIntOrNull()
        val authority = (binding.authorityChips.children.firstOrNull { (it as? Chip)?.isChecked == true } as? Chip)?.tag?.toString()
        val practiceYear = (binding.practiceYearChips.children.firstOrNull { (it as? Chip)?.isChecked == true } as? Chip)?.tag?.toString()?.toIntOrNull()
        return SearchFilters(
            types = selectedTypes.ifEmpty { null },
            docIds = selectedDocIds.ifEmpty { null },
            court = court,
            caseYear = caseYear,
            authority = authority,
            practiceYear = practiceYear,
        )
    }

    private suspend fun renderFacets(res: io.github.stcksmsh.pravnik.domain.repo.SearchResult, filters: SearchFilters) {
        fun chipGroup(group: com.google.android.material.chip.ChipGroup, entries: List<Pair<String,String>>, singleSelection: Boolean = true) {
            group.removeAllViews()
            group.isSingleSelection = singleSelection
            entries.forEach { (label, key) ->
                val chip = layoutInflater.inflate(R.layout.view_choice_chip, group, false) as Chip
                chip.text = label
                chip.tag = key
                chip.isCheckable = true
                chip.isChecked = false
                chip.setOnCheckedChangeListener { _, _ -> scheduleSearch(binding.queryInput.text?.toString()) }
                group.addView(chip)
            }
        }
        // Preload docs map for titles
        val docs = runCatching { documentsRepo.listAll() }.getOrNull().orEmpty().associateBy { it.id }
        val topDocIds = res.facets.docCounts.entries.sortedByDescending { it.value }.take(10).map { it.key }
        val docEntries = topDocIds.map { id -> (docs[id]?.title ?: id) to id }
        chipGroup(binding.docChips, docEntries, singleSelection = false)
        chipGroup(binding.courtChips, res.facets.courts.keys.sorted().map { it to it })
        chipGroup(binding.caseYearChips, res.facets.caseYears.keys.sortedDescending().map { it.toString() to it.toString() })
        chipGroup(binding.authorityChips, res.facets.authorities.keys.sorted().map { it to it })
        chipGroup(binding.practiceYearChips, res.facets.practiceYears.keys.sortedDescending().map { it.toString() to it.toString() })
    }

    data class UiRow(val docId: String, val anchor: String, val docTitle: String, val unitTitle: String?, val snippet: String, val query: String)

    class ResultsAdapter(private val onClick: (UiRow) -> Unit) : ListAdapter<UiRow, ResultsVH>(object : DiffUtil.ItemCallback<UiRow>() {
        override fun areItemsTheSame(oldItem: UiRow, newItem: UiRow) = oldItem === newItem
        override fun areContentsTheSame(oldItem: UiRow, newItem: UiRow) = oldItem == newItem
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsVH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_row, parent, false)
            return ResultsVH(v, onClick)
        }

        override fun onBindViewHolder(holder: ResultsVH, position: Int) = holder.bind(getItem(position))
    }

    class ResultsVH(view: View, private val onClick: (UiRow) -> Unit) : RecyclerView.ViewHolder(view) {
        private val title: android.widget.TextView = view.findViewById(R.id.title)
        private val snippet: android.widget.TextView = view.findViewById(R.id.snippet)
        init { snippet.movementMethod = LinkMovementMethod.getInstance() }
        fun bind(item: UiRow) {
            itemView.setOnClickListener { onClick(item) }
            title.text = item.unitTitle ?: item.docTitle
            val tokens = RenderUtils.tokenizeQuery(item.query)
            val highlighted = RenderUtils.highlightQuery(
                item.snippet.replace("<b>", "").replace("</b>", ""),
                tokens,
                0x66FFEB3B.toInt()
            )
            snippet.text = RenderUtils.applyClickableArticleRefs(highlighted.toString()) { number ->
                object : android.text.style.ClickableSpan() { override fun onClick(widget: View) { /* in-snippet ref click noop here */ } }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
