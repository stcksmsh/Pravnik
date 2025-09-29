package io.github.stcksmsh.pravnik.ui.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StarredFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var documentsRepo: DocumentsRepo

    private val adapter = SimpleAdapter { item ->
        val action = StarredFragmentDirections.actionHomeToDocument(item.tag as String)
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
            val docs = documentsRepo.listStarred()
            adapter.submitList(docs.map { SimpleItem(it.title, it.id) })
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

data class SimpleItem(val title: String, val tag: Any?)

class SimpleAdapter(private val onClick: (SimpleItem) -> Unit) : ListAdapter<SimpleItem, VH>(object : DiffUtil.ItemCallback<SimpleItem>() {
    override fun areItemsTheSame(oldItem: SimpleItem, newItem: SimpleItem) = oldItem === newItem
    override fun areContentsTheSame(oldItem: SimpleItem, newItem: SimpleItem) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return VH(v, onClick)
    }
    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}

class VH(view: View, private val onClick: (SimpleItem) -> Unit) : RecyclerView.ViewHolder(view) {
    private val title: android.widget.TextView = view.findViewById(android.R.id.text1)
    fun bind(item: SimpleItem) { title.text = item.title; itemView.setOnClickListener { onClick(item) } }
}
