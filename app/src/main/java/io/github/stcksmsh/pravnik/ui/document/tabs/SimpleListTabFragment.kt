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

    private var notes: List<io.github.stcksmsh.pravnik.domain.model.Note> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val docId = requireArguments().getString("docId")!!
        val mode = requireArguments().getString("mode") // defs | tariffs | notes | meta | citations
        val listView: android.widget.ListView = view.findViewById(R.id.list)
        val addFab: com.google.android.material.floatingactionbutton.FloatingActionButton = view.findViewById(R.id.addFab)
        addFab.visibility = if (mode == "notes") View.VISIBLE else View.GONE
        addFab.setOnClickListener { promptAddNote(docId) }
        if (mode == "notes") {
            listView.setOnItemClickListener { _, _, position, _ ->
                val note = notes.getOrNull(position) ?: return@setOnItemClickListener
                showNoteActions(docId, note)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            when (mode) {
                "defs" -> {
                    val items = definitionsRepo.listForDoc(docId).map { it.term + ": " + it.text }
                    listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
                "tariffs" -> {
                    val items = tariffsRepo.listForDoc(docId).map { (it.title ?: it.key) + (it.amount?.let { a -> " — $a" } ?: "") }
                    listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
                "notes" -> loadNotes(docId, listView)
                "meta" -> {
                    val case = caseMetaRepo.get(docId)
                    val practice = practiceMetaRepo.get(docId)
                    val items = when {
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
                    listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
                "citations" -> {
                    val from = citatorRepo.listFrom(docId).map { "Cites → ${'$'}{it.toDocId}" }
                    val to = citatorRepo.listTo(docId).map { "Cited by ← ${'$'}{it.fromDocId}" }
                    val items = from + to
                    listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
                }
                else -> listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, emptyList<String>())
            }

        }
    }
    
    private suspend fun loadNotes(docId: String, listView: android.widget.ListView) {
        notes = notesRepo.listForDoc(docId)
        listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, notes.map { it.body })
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
                            unitAnchor = "",
                            title = null,
                            body = body,
                            updatedAt = System.currentTimeMillis()
                        ))
                        view?.findViewById<android.widget.ListView>(R.id.list)?.let { list ->
                            loadNotes(docId, list)
                        }
                    }
                }
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showNoteActions(docId: String, note: io.github.stcksmsh.pravnik.domain.model.Note) {
        val ctx = requireContext()
        val options = arrayOf(getString(R.string.edit), getString(R.string.delete))
        com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
            .setItems(options) { d, which ->
                when (which) {
                    0 -> promptEditNote(docId, note)
                    1 -> {
                        viewLifecycleOwner.lifecycleScope.launch {
                            notesRepo.delete(note.id)
                            view?.findViewById<android.widget.ListView>(R.id.list)?.let { list ->
                                loadNotes(docId, list)
                            }
                        }
                    }
                }
                d.dismiss()
            }
            .show()
    }

    private fun promptEditNote(docId: String, note: io.github.stcksmsh.pravnik.domain.model.Note) {
        val ctx = requireContext()
        val input = android.widget.EditText(ctx).apply { setText(note.body) }
        com.google.android.material.dialog.MaterialAlertDialogBuilder(ctx)
            .setTitle(R.string.edit)
            .setView(input)
            .setPositiveButton(R.string.save) { d, _ ->
                val body = input.text?.toString()?.trim().orEmpty()
                if (body.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        notesRepo.upsert(note.copy(body = body, updatedAt = System.currentTimeMillis()))
                        view?.findViewById<android.widget.ListView>(R.id.list)?.let { list ->
                            loadNotes(docId, list)
                        }
                    }
                }
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
