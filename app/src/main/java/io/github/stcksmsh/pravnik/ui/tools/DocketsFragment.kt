package io.github.stcksmsh.pravnik.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.model.Docket
import io.github.stcksmsh.pravnik.domain.repo.DocketsRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DocketsFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var repo: DocketsRepo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            val items = repo.listDockets()
            val titles = items.map { it.caseName }
            binding.list.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, titles)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class DocketsAdapter : ListAdapter<Docket, DocketsVH>(object : DiffUtil.ItemCallback<Docket>() {
    override fun areItemsTheSame(oldItem: Docket, newItem: Docket) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Docket, newItem: Docket) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocketsVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return DocketsVH(v)
    }
    override fun onBindViewHolder(holder: DocketsVH, position: Int) = holder.bind(getItem(position))
}

class DocketsVH(view: View) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: Docket) { t1.text = item.caseName; t2.text = item.caseNumber ?: "" }
}
