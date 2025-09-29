package io.github.stcksmsh.pravnik.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            vm.docs.collectLatest { docs ->
                binding.emptyView.isVisible = docs.isEmpty()
                binding.docsCount.text = getString(R.string.home_docs_count, docs.size)
            }
        }
        binding.btnOpenSearch.setOnClickListener { findNavController().navigate(R.id.action_home_to_search) }
        listOf(
            binding.chipConstitution to DocumentType.CONSTITUTION,
            binding.chipLaw to DocumentType.LAW,
            binding.chipBylaw to DocumentType.BYLAW,
            binding.chipCase to DocumentType.CASE,
            binding.chipPractice to DocumentType.PRACTICE,
        ).forEach { (chip, type) ->
            chip.setOnCheckedChangeListener { _, _ -> vm.toggleType(type) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
