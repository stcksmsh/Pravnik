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
import io.github.stcksmsh.pravnik.domain.repo.CaseMetaRepo
import io.github.stcksmsh.pravnik.domain.repo.PracticeMetaRepo
import io.github.stcksmsh.pravnik.domain.repo.CitatorRepo
import io.github.stcksmsh.pravnik.domain.repo.NotesRepo
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SimpleListTabFragment : Fragment() {
    @Inject lateinit var definitionsRepo: DefinitionsRepo
    @Inject lateinit var tariffsRepo: TariffsRepo
    @Inject lateinit var caseMetaRepo: CaseMetaRepo
    @Inject lateinit var practiceMetaRepo: PracticeMetaRepo
    @Inject lateinit var citatorRepo: CitatorRepo
    @Inject lateinit var notesRepo: NotesRepo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_simple_list_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val docId = requireArguments().getString("docId")!!
        val mode = requireArguments().getString("mode") // defs | tariffs | notes | meta | citations
        val listView: android.widget.ListView = view.findViewById(R.id.list)
        val addFab: com.google.android.material.floatingactionbutton.FloatingActionButton = view.findViewById(R.id.addFab)
        if (mode == "notes") addFab.visibility = View.VISIBLE else addFab.visibility = View.GONE
        addFab.setOnClickListener { promptAddNote(docId) }
        viewLifecycleOwner.lifecycleScope.launch {
            val items: List<String> = when (mode) {
                "defs" -> definitionsRepo.listForDoc(docId).map { it.term + ": " + it.text }
                "tariffs" -> tariffsRepo.listForDoc(docId).map { (it.title ?: it.key) + (it.amount?.let { a -> " — $a" } ?: "") }
                "notes" -> notesRepo.listForDoc(docId).map { it.body }
                "meta" -> {
                    val case = caseMetaRepo.get(docId)
                    val practice = practiceMetaRepo.get(docId)
                    when {
                        case != null -> listOfNotNull(
                            "Court: ${'$'}{case.court}",
                            "Case no: ${'$'}{case.caseNumber}",
                            "Decided: ${'$'}{case.decidedOn}",
                            "Outcome: ${'$'}{case.outcome}",
                        )
                        practice != null -> listOfNotNull(
                            "Authority: ${'$'}{practice.authority}",
                            "Doc no: ${'$'}{practice.documentNumber}",
                            "Issued: ${'$'}{practice.issuedOn}",
                        )
                        else -> emptyList()
                    }
                }
                "citations" -> {
                    val from = citatorRepo.listFrom(docId).map { "Cites → ${'$'}{it.toId}" }
                    val to = citatorRepo.listTo(docId).map { "Cited by ← ${'$'}{it.fromId}" }
                    from + to
                }
                else -> emptyList()
            }
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)

        }
    }

    private fun promptAddNote(docId: String) {
        val ctx = requireContext()
        val input = android.widget.EditText(ctx).apply { hint = getString(R.string.note_hint) }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.add_note)
            .setView(input)
            .setPositiveButton(R.string.save) { d, _ ->
                val body = input.text?.toString()?.trim().orEmpty()
                if (body.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        notesRepo.upsert(io.github.stcksmsh.pravnik.domain.model.Note(
                            id = 0,
                            docId = docId,
                            unitAnchor = "", // TODO: wire current anchor if available
                            title = null,
                            body = body,
                            updatedAt = System.currentTimeMillis()
                        ))
                        // refresh
                        view?.findViewById<android.widget.ListView>(R.id.list)?.let { list ->
                            val items = notesRepo.listForDoc(docId).map { it.body }
                            list.adapter = android.widget.ArrayAdapter(ctx, android.R.layout.simple_list_item_1, items)
                        }
                    }
                }
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
