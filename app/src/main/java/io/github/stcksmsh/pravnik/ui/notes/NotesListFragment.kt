package io.github.stcksmsh.pravnik.ui.notes

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
import io.github.stcksmsh.pravnik.domain.repo.NotesRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotesListFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var notesRepo: NotesRepo

    private val adapter = NotesAdapter { item ->
        val action = NotesListFragmentDirections.actionHomeToDocument(item.docId, item.unitAnchor)
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
            val notes = notesRepo.listAll()
            adapter.submitList(notes)
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

class NotesAdapter(private val onClick: (io.github.stcksmsh.pravnik.domain.model.Note) -> Unit) : ListAdapter<io.github.stcksmsh.pravnik.domain.model.Note, NotesVH>(object : DiffUtil.ItemCallback<io.github.stcksmsh.pravnik.domain.model.Note>() {
    override fun areItemsTheSame(oldItem: io.github.stcksmsh.pravnik.domain.model.Note, newItem: io.github.stcksmsh.pravnik.domain.model.Note) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: io.github.stcksmsh.pravnik.domain.model.Note, newItem: io.github.stcksmsh.pravnik.domain.model.Note) = oldItem == newItem
}) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesVH {
        val v = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return NotesVH(v, onClick)
    }
    override fun onBindViewHolder(holder: NotesVH, position: Int) = holder.bind(getItem(position))
}

class NotesVH(view: View, private val onClick: (io.github.stcksmsh.pravnik.domain.model.Note) -> Unit) : RecyclerView.ViewHolder(view) {
    private val t1: android.widget.TextView = view.findViewById(android.R.id.text1)
    private val t2: android.widget.TextView = view.findViewById(android.R.id.text2)
    fun bind(item: io.github.stcksmsh.pravnik.domain.model.Note) {
        t1.text = item.title ?: item.body.take(40)
        t2.text = "#" + item.unitAnchor
        itemView.setOnClickListener { onClick(item) }
    }
}
