package io.github.stcksmsh.pravnik.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import io.github.stcksmsh.pravnik.domain.repo.HistoryRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var historyRepo: HistoryRepo
    @Inject lateinit var documentsRepo: DocumentsRepo

    private val adapter = HistoryAdapter { item ->
        val action = HistoryFragmentDirections.actionHomeToDocument(item.docId, item.unitAnchor)
        findNavController().navigate(action)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            val items = historyRepo.recent(100)
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class HistoryItem(val docId: String, val unitAnchor: String, val visitedAt: Long)

class HistoryAdapter(private val onClick: (HistoryItem) -> Unit) : ListAdapter<io.github.stcksmsh.pravnik.domain.model.History, HistoryVH>(object : DiffUtil.ItemCallback<io.github.stcksmsh.pravnik.domain.model.History>() {
    override fun areItemsTheSame(oldItem: io.github.stcksmsh.pravnik.domain.model.History, newItem: io.github.stcksmsh.pravnik.domain.model.History) = oldItem === newItem
    override fun areContentsTheSame(oldItem: io.github.stcksmsh.pravnik.domain.model.History, newItem: io.github.stcksmsh.pravnik.domain.model.History) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return HistoryVH(v, onClick)
    }
    override fun onBindViewHolder(holder: HistoryVH, position: Int) = holder.bind(getItem(position))
}

class HistoryVH(view: View, private val onClick: (HistoryItem) -> Unit) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: io.github.stcksmsh.pravnik.domain.model.History) {
        t1.text = item.docId
        t2.text = "#" + item.unitAnchor + " — " + java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.visitedAt))
        itemView.setOnClickListener { onClick(HistoryItem(item.docId, item.unitAnchor, item.visitedAt)) }
    }
}
