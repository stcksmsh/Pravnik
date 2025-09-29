package io.github.stcksmsh.pravnik.ui.starred

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
        viewLifecycleOwner.lifecycleScope.launch {
            val docs = documentsRepo.listStarred()
            val titles = docs.map { it.title }
            val ids = docs.map { it.id }
            binding.list.adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, titles)
            binding.list.setOnItemClickListener { _, _, pos, _ ->
                val docId = ids[pos]
                val action = io.github.stcksmsh.pravnik.NavGraphDirections.actionGlobalDocumentFragment(docId, "")
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
