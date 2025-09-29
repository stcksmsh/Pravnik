package io.github.stcksmsh.pravnik.ui.search

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentSearchBinding
import io.github.stcksmsh.pravnik.domain.model.DocumentType
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

    private val adapter = ResultsAdapter()
    private var job: Job? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resultsList.adapter = adapter
        binding.queryInput.addTextChangedListener { text ->
            job?.cancel()
            job = viewLifecycleOwner.lifecycleScope.launch {
                delay(250)
                val q = text?.toString()?.trim().orEmpty()
                if (q.isNotEmpty()) performSearch(q) else adapter.submitList(emptyList())
            }
        }
    }

    private suspend fun performSearch(q: String) {
        val res = searchRepo.search(q, SearchFilters(types = setOf(DocumentType.LAW, DocumentType.CONSTITUTION, DocumentType.BYLAW, DocumentType.CASE, DocumentType.PRACTICE)))
        adapter.submitList(res.rows.map { row -> UiRow(row.docTitle, row.unitTitle, row.snippetHtml, q) })
    }

    data class UiRow(val docTitle: String, val unitTitle: String?, val snippet: String, val query: String)

    class ResultsAdapter : ListAdapter<UiRow, ResultsVH>(object : DiffUtil.ItemCallback<UiRow>() {
        override fun areItemsTheSame(oldItem: UiRow, newItem: UiRow) = oldItem === newItem
        override fun areContentsTheSame(oldItem: UiRow, newItem: UiRow) = oldItem == newItem
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultsVH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_row, parent, false)
            return ResultsVH(v)
        }

        override fun onBindViewHolder(holder: ResultsVH, position: Int) = holder.bind(getItem(position))
    }

    class ResultsVH(view: View) : RecyclerView.ViewHolder(view) {
        private val title: android.widget.TextView = view.findViewById(R.id.title)
        private val snippet: android.widget.TextView = view.findViewById(R.id.snippet)
        init { snippet.movementMethod = LinkMovementMethod.getInstance() }
        fun bind(item: UiRow) {
            title.text = item.unitTitle ?: item.docTitle
            val tokens = RenderUtils.tokenizeQuery(item.query)
            val highlighted = RenderUtils.highlightQuery(
                item.snippet.replace("<b>", "").replace("</b>", ""),
                tokens,
                0x66FFEB3B.toInt()
            )
            snippet.text = RenderUtils.applyClickableArticleRefs(highlighted.toString()) { number ->
                object : android.text.style.ClickableSpan() { override fun onClick(widget: View) { /* TODO: navigate */ } }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
