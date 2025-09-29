package io.github.stcksmsh.pravnik.ui.data

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.stcksmsh.pravnik.domain.repo.*

import dagger.hilt.android.AndroidEntryPoint
import io.github.stcksmsh.pravnik.databinding.FragmentImportExportBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class ImportExportFragment : Fragment() {
    @javax.inject.Inject lateinit var bookmarksRepo: BookmarksRepo
    @javax.inject.Inject lateinit var notesRepo: NotesRepo
    @javax.inject.Inject lateinit var historyRepo: HistoryRepo
    @javax.inject.Inject lateinit var savedRepo: SavedSearchesRepo
    @javax.inject.Inject lateinit var collectionsRepo: CollectionsRepo
    @javax.inject.Inject lateinit var templatesRepo: TemplatesRepo
    @javax.inject.Inject lateinit var checklistsRepo: ChecklistsRepo
    @javax.inject.Inject lateinit var docketsRepo: DocketsRepo

    private var _binding: FragmentImportExportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImportExportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnExport.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { doExport() } }
        binding.btnImport.setOnClickListener { viewLifecycleOwner.lifecycleScope.launch { doImport() } }
    }

    private suspend fun doExport() {
        withContext(Dispatchers.IO) {
            val ctx = requireContext().applicationContext
            val file = java.io.File(ctx.filesDir, "pravnik-export.json")
            val data = exportUserData()
            file.writeText(data)
            withContext(Dispatchers.Main) {
                binding.tvStatus.text = "Exported to: ${'$'}{file.absolutePath}"
            }
        }
    }

    private suspend fun doImport() {
        withContext(Dispatchers.IO) {
            val ctx = requireContext().applicationContext
            val file = java.io.File(ctx.filesDir, "pravnik-export.json")
            if (file.exists()) {
                val json = file.readText()
                importUserData(json)
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Imported from: ${'$'}{file.absolutePath} (size ${'$'}{json.length} bytes)"
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "No export file found."
                }
            }
    private suspend fun exportUserData(): String {
        val bookmarks = bookmarksRepo.listBookmarks()
        val notes = notesRepo.listAll()
        val history = historyRepo.recent(10000)
        val saved = savedRepo.listSaved()
        val collections = collectionsRepo.listCollections()
        val templates = templatesRepo.listTemplates()
        val checklists = checklistsRepo.listChecklists()
        val dockets = docketsRepo.listDockets()

        val root = org.json.JSONObject()
        root.put("bookmarks", org.json.JSONArray(bookmarks.map { org.json.JSONObject().apply {
            put("id", it.id); put("folderId", it.folderId); put("docId", it.docId); put("unitAnchor", it.unitAnchor)
            put("title", it.title); put("note", it.note); put("tagsCsv", it.tagsCsv); put("createdAt", it.createdAt)
        } }))
        root.put("notes", org.json.JSONArray(notes.map { org.json.JSONObject().apply {
            put("id", it.id); put("docId", it.docId); put("unitAnchor", it.unitAnchor); put("title", it.title)
            put("body", it.body); put("updatedAt", it.updatedAt)
        } }))
        root.put("history", org.json.JSONArray(history.map { org.json.JSONObject().apply {
            put("id", it.id); put("docId", it.docId); put("unitAnchor", it.unitAnchor); put("visitedAt", it.visitedAt)
        } }))
        root.put("savedSearches", org.json.JSONArray(saved.map { org.json.JSONObject().apply {
            put("id", it.id); put("query", it.query); put("filtersJson", it.filtersJson); put("createdAt", it.createdAt); put("notify", it.notify)
        } }))
        root.put("collections", org.json.JSONArray(collections.map { org.json.JSONObject().apply {
            put("id", it.id); put("name", it.name); put("createdAt", it.createdAt)
        } }))
        root.put("templates", org.json.JSONArray(templates.map { org.json.JSONObject().apply {
            put("id", it.id); put("title", it.title); put("language", it.language); put("bodyMd", it.bodyMd); put("tagsCsv", it.tagsCsv); put("createdAt", it.createdAt)
        } }))
        root.put("checklists", org.json.JSONArray(checklists.map { org.json.JSONObject().apply {
            put("id", it.id); put("title", it.title); put("docId", it.docId); put("unitAnchor", it.unitAnchor); put("itemsJson", it.itemsJson); put("createdAt", it.createdAt)
        } }))
        root.put("dockets", org.json.JSONArray(dockets.map { org.json.JSONObject().apply {
            put("id", it.id); put("caseName", it.caseName); put("caseNumber", it.caseNumber); put("court", it.court); put("notes", it.notes); put("createdAt", it.createdAt)
        } }))

        return root.toString()
    }

    private suspend fun importUserData(json: String) {
        val root = org.json.JSONObject(json)

        root.optJSONArray("bookmarks")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                bookmarksRepo.upsertBookmark(io.github.stcksmsh.pravnik.domain.model.Bookmark(
                    id = o.optLong("id"), folderId = if (o.isNull("folderId")) null else o.optLong("folderId"),
                    docId = o.getString("docId"), unitAnchor = o.getString("unitAnchor"), title = o.optString("title", null),
                    note = o.optString("note", null), tagsCsv = o.optString("tagsCsv", null), createdAt = o.getLong("createdAt")
                ))
            }
        }

        root.optJSONArray("notes")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                notesRepo.upsert(io.github.stcksmsh.pravnik.domain.model.Note(
                    id = o.optLong("id"), docId = o.getString("docId"), unitAnchor = o.getString("unitAnchor"),
                    title = o.optString("title", null), body = o.getString("body"), updatedAt = o.getLong("updatedAt")
                ))
            }
        }

        root.optJSONArray("history")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                historyRepo.insert(io.github.stcksmsh.pravnik.domain.model.History(
                    id = o.optLong("id"), docId = o.getString("docId"), unitAnchor = o.getString("unitAnchor"), visitedAt = o.getLong("visitedAt")
                ))
            }
        }

        root.optJSONArray("savedSearches")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                savedRepo.upsert(io.github.stcksmsh.pravnik.domain.model.SavedSearch(
                    id = o.optLong("id"), query = o.getString("query"), filtersJson = o.optString("filtersJson", null),
                    createdAt = o.getLong("createdAt"), notify = o.optBoolean("notify", false)
                ))
            }
        }

        root.optJSONArray("collections")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                collectionsRepo.upsertCollection(io.github.stcksmsh.pravnik.domain.model.Collection(
                    id = o.optLong("id"), name = o.getString("name"), createdAt = o.getLong("createdAt")
                ))
            }
        }

        root.optJSONArray("templates")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                templatesRepo.upsert(io.github.stcksmsh.pravnik.domain.model.Template(
                    id = o.optLong("id"), title = o.getString("title"), language = o.getString("language"), bodyMd = o.getString("bodyMd"),
                    tagsCsv = o.optString("tagsCsv", null), createdAt = o.getLong("createdAt")
                ))
            }
        }

        root.optJSONArray("checklists")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                checklistsRepo.upsert(io.github.stcksmsh.pravnik.domain.model.Checklist(
                    id = o.optLong("id"), title = o.getString("title"), docId = o.optString("docId", null), unitAnchor = o.optString("unitAnchor", null),
                    itemsJson = o.getString("itemsJson"), createdAt = o.getLong("createdAt")
                ))
            }
        }

        root.optJSONArray("dockets")?.let { arr ->
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                docketsRepo.upsertDocket(io.github.stcksmsh.pravnik.domain.model.Docket(
                    id = o.optLong("id"), caseName = o.getString("caseName"), caseNumber = o.optString("caseNumber", null),
                    court = o.optString("court", null), notes = o.optString("notes", null), createdAt = o.getLong("createdAt")
                ))
            }
        }
    }

                // TODO: deserialize and import into repos
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "Imported from: ${'$'}{file.absolutePath} (size ${'$'}{json.length} bytes)"
                }
            } else {
                withContext(Dispatchers.Main) {
                    binding.tvStatus.text = "No export file found."
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
