package io.github.stcksmsh.pravnik.ui.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentSimpleListTabBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImportExportFragment : Fragment() {
    private var _binding: FragmentSimpleListTabBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSimpleListTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.addFab.visibility = View.GONE
        binding.list.visibility = View.GONE
        binding.root.findViewById<android.widget.TextView>(io.github.stcksmsh.pravnik.R.id.emptyView)?.apply {
            visibility = View.VISIBLE
            text = "Import/Export coming soon"
        }
    }

    private suspend fun exportAll(): java.io.File = withContext(Dispatchers.IO) {
        val ctx = requireContext().applicationContext
        val file = java.io.File(ctx.filesDir, "pravnik-export.json")
        // TODO: serialize user data repos here
        file.writeText("{}")
        file
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
