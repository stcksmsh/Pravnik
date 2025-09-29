package io.github.stcksmsh.pravnik.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.sectionSavedSearches.findViewById<android.widget.TextView>(R.id.title).text = getString(R.string.home_saved_searches)
        binding.sectionBookmarks.findViewById<android.widget.TextView>(R.id.title).text = getString(R.string.home_bookmarks)
        binding.sectionHistory.findViewById<android.widget.TextView>(R.id.title).text = getString(R.string.home_history)
        binding.sectionStarred.findViewById<android.widget.TextView>(R.id.title).text = getString(R.string.home_starred)

        binding.sectionSavedSearches.findViewById<android.view.View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToSavedSearches()) }
        binding.sectionBookmarks.findViewById<android.view.View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToBookmarks()) }
        binding.sectionHistory.findViewById<android.view.View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToHistory()) }
        binding.sectionStarred.findViewById<android.view.View>(R.id.seeAll).setOnClickListener { findNavController().navigate(HomeFragmentDirections.actionHomeToStarred()) }

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

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.docs.collectLatest { docs ->
                binding.emptyView.isVisible = docs.isEmpty()
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
