package io.github.stcksmsh.pravnik.ui.tools

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
import io.github.stcksmsh.pravnik.domain.model.Checklist
import io.github.stcksmsh.pravnik.domain.repo.ChecklistsRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChecklistsFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var repo: ChecklistsRepo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext())
        val adapter = ChecklistsAdapter()
        binding.list.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.submitList(repo.listChecklists())
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class ChecklistsAdapter : ListAdapter<Checklist, ChecklistsVH>(object : DiffUtil.ItemCallback<Checklist>() {
    override fun areItemsTheSame(oldItem: Checklist, newItem: Checklist) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Checklist, newItem: Checklist) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistsVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ChecklistsVH(v)
    }
    override fun onBindViewHolder(holder: ChecklistsVH, position: Int) = holder.bind(getItem(position))
}

class ChecklistsVH(view: View) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: Checklist) { t1.text = item.title; t2.text = item.docId ?: "" }
}
