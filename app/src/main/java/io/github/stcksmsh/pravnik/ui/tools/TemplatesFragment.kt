package io.github.stcksmsh.pravnik.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.model.Template
import io.github.stcksmsh.pravnik.domain.repo.TemplatesRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TemplatesFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var repo: TemplatesRepo


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            val items = repo.listTemplates()
            val titles = items.map { it.title }
            binding.list.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, titles)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class TemplatesAdapter : ListAdapter<Template, TemplatesVH>(object : DiffUtil.ItemCallback<Template>() {
    override fun areItemsTheSame(oldItem: Template, newItem: Template) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Template, newItem: Template) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplatesVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return TemplatesVH(v)
    }
    override fun onBindViewHolder(holder: TemplatesVH, position: Int) = holder.bind(getItem(position))
}

class TemplatesVH(view: View) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: Template) { t1.text = item.title; t2.text = item.language }
}
