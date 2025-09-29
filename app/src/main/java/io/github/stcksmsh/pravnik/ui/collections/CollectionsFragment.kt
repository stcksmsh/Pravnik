package io.github.stcksmsh.pravnik.ui.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.model.Collection
import io.github.stcksmsh.pravnik.domain.repo.CollectionsRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CollectionsFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var collectionsRepo: CollectionsRepo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        val adapter = CollectionsAdapter { /* TODO: open collection details */ }
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.submitList(collectionsRepo.listCollections())
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class CollectionsAdapter(private val onClick: (Collection) -> Unit) : ListAdapter<Collection, CollectionsVH>(object : DiffUtil.ItemCallback<Collection>() {
    override fun areItemsTheSame(oldItem: Collection, newItem: Collection) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Collection, newItem: Collection) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionsVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return CollectionsVH(v, onClick)
    }
    override fun onBindViewHolder(holder: CollectionsVH, position: Int) = holder.bind(getItem(position))
}

class CollectionsVH(view: View, private val onClick: (Collection) -> Unit) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    fun bind(item: Collection) { t1.text = item.name; itemView.setOnClickListener { onClick(item) } }
}
