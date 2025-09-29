package io.github.stcksmsh.pravnik.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.databinding.FragmentHomeBinding
import io.github.stcksmsh.pravnik.domain.model.DocumentType
import kotlinx.coroutines.flow.collectLatest
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.load()

        // Section headers and See all
        binding.sectionSavedSearches.root.findViewById<TextView>(R.id.title).text = getString(R.string.home_recent_searches)
        binding.sectionBookmarks.root.findViewById<TextView>(R.id.title).text = getString(R.string.home_bookmarks)
        binding.sectionHistory.root.findViewById<TextView>(R.id.title).text = getString(R.string.home_history)
        binding.sectionStarred.root.findViewById<TextView>(R.id.title).text = getString(R.string.home_starred)

        binding.sectionSavedSearches.root.findViewById<View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToSavedSearches()) }
        binding.sectionBookmarks.root.findViewById<View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToBookmarks()) }
        binding.sectionHistory.root.findViewById<View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToHistory()) }
        binding.sectionStarred.root.findViewById<View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToStarred()) }

        // Search box opens full search
        binding.editSearch.setOnFocusChangeListener { _, hasFocus -> if (hasFocus) findNavController().navigate(HomeFragmentDirections.actionHomeToSearch()) }

        // Type chips
        val cg = binding.typeChips
        cg.removeAllViews()
        listOf(DocumentType.LAW, DocumentType.CONSTITUTION, DocumentType.BYLAW, DocumentType.CASE, DocumentType.PRACTICE).forEach { t ->
            val chip = layoutInflater.inflate(R.layout.view_choice_chip, cg, false) as com.google.android.material.chip.Chip
            chip.text = t.name
            chip.isCheckable = true
            chip.isChecked = true
            cg.addView(chip)
        }

        // Setup section lists
        binding.recentSearchesList.layoutManager = LinearLayoutManager(requireContext())
        binding.recentBookmarksList.layoutManager = LinearLayoutManager(requireContext())
        binding.recentHistoryList.layoutManager = LinearLayoutManager(requireContext())
        binding.recentStarredList.layoutManager = LinearLayoutManager(requireContext())

        val queriesAdapter = SimpleTextAdapter(HomeBinders.query) { q ->
            val args = android.os.Bundle().apply { putString("initialQuery", q.query) }
            findNavController().navigate(R.id.searchFragment, args)
        }
        val bookmarksAdapter = SimpleTextAdapter(HomeBinders.bookmark) { b ->
            val action = HomeFragmentDirections.actionHomeToDocument(b.docId, b.unitAnchor)
            findNavController().navigate(action)
        }
        val historyAdapter = SimpleTextAdapter(HomeBinders.history) { h ->
            val action = HomeFragmentDirections.actionHomeToDocument(h.docId, h.unitAnchor)
            findNavController().navigate(action)
        }
        val starredAdapter = SimpleTextAdapter(HomeBinders.document) { doc ->
            val action = HomeFragmentDirections.actionHomeToDocument(doc.id, "")
            findNavController().navigate(action)
        }
        binding.recentSearchesList.adapter = queriesAdapter
        binding.recentBookmarksList.adapter = bookmarksAdapter
        binding.recentHistoryList.adapter = historyAdapter
        binding.recentStarredList.adapter = starredAdapter

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.docs.collectLatest { docs -> binding.emptyView.isVisible = docs.isEmpty() }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.recentQueries.collectLatest {
                binding.emptySavedSearches.isVisible = it.isEmpty()
                binding.sectionSavedSearches.root.isVisible = it.isNotEmpty()
                binding.recentSearchesList.isVisible = it.isNotEmpty()
                queriesAdapter.submitList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.recentBookmarks.collectLatest {
                binding.emptyBookmarks.isVisible = it.isEmpty()
                binding.sectionBookmarks.root.isVisible = it.isNotEmpty()
                binding.recentBookmarksList.isVisible = it.isNotEmpty()
                bookmarksAdapter.submitList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.recentHistory.collectLatest {
                binding.emptyHistory.isVisible = it.isEmpty()
                binding.sectionHistory.root.isVisible = it.isNotEmpty()
                binding.recentHistoryList.isVisible = it.isNotEmpty()
                historyAdapter.submitList(it)
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.starredDocs.collectLatest {
                binding.emptyStarred.isVisible = it.isEmpty()
                binding.sectionStarred.root.isVisible = it.isNotEmpty()
                binding.recentStarredList.isVisible = it.isNotEmpty()
                starredAdapter.submitList(it)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
