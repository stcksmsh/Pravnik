package io.github.stcksmsh.pravnik.ui.document.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.R
import io.github.stcksmsh.pravnik.domain.repo.DefinitionsRepo
import io.github.stcksmsh.pravnik.domain.repo.TariffsRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimpleListTabFragment : Fragment() {
    @Inject lateinit var definitionsRepo: DefinitionsRepo
    @Inject lateinit var tariffsRepo: TariffsRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_simple_list_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val docId = requireArguments().getString("docId")!!
        val mode = requireArguments().getString("mode") // defs | tariffs | notes | meta | citations
        val listView: android.widget.ListView = view.findViewById(R.id.list)
        viewLifecycleOwner.lifecycleScope.launch {
            val items: List<String> = when (mode) {
                "defs" -> definitionsRepo.listForDoc(docId).map { it.term + ": " + it.text }
                "tariffs" -> tariffsRepo.listForDoc(docId).map { (it.title ?: it.key) + (it.amount?.let { a -> " — $a" } ?: "") }
                else -> emptyList()
            }
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        }
    }
}
