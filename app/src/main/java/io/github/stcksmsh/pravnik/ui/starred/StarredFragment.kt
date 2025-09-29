package io.github.stcksmsh.pravnik.ui.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StarredFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var documentsRepo: DocumentsRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        viewLifecycleOwner.lifecycleScope.launch {
            val docs = documentsRepo.listStarred()
            binding.list.adapter = object : RecyclerView.Adapter<SimpleDocVH>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleDocVH {
                    val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                    return SimpleDocVH(v) { pos ->
                        val docId = docs[pos].id
                        val action = io.github.stcksmsh.pravnik.NavGraphDirections.actionGlobalDocumentFragment(docId, "")
                        findNavController().navigate(action)
                    }
                }
                override fun getItemCount() = docs.size
                override fun onBindViewHolder(holder: SimpleDocVH, position: Int) { holder.bind(docs[position].title) }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class SimpleDocVH(view: View, private val onClick: (Int) -> Unit) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    fun bind(title: String) { t1.text = title; itemView.setOnClickListener { onClick(bindingAdapterPosition) } }
}

