package io.github.stcksmsh.pravnik.ui.saved

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
import io.github.stcksmsh.pravnik.domain.model.SavedSearch
import io.github.stcksmsh.pravnik.domain.repo.SavedSearchesRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SavedSearchesFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var savedRepo: SavedSearchesRepo

    private val adapter = SavedAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            val items = savedRepo.listSaved()
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class SavedAdapter : ListAdapter<SavedSearch, SavedVH>(object : DiffUtil.ItemCallback<SavedSearch>() {
    override fun areItemsTheSame(oldItem: SavedSearch, newItem: SavedSearch) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: SavedSearch, newItem: SavedSearch) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return SavedVH(v)
    }
    override fun onBindViewHolder(holder: SavedVH, position: Int) = holder.bind(getItem(position))
}

class SavedVH(view: View) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: SavedSearch) {
        t1.text = item.query
        t2.text = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.createdAt))
    }
}
