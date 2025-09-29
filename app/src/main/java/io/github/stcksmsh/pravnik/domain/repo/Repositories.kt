package io.github.stcksmsh.pravnik.domain.repo

import androidx.sqlite.db.SupportSQLiteQuery
import io.github.stcksmsh.pravnik.domain.model.*
import io.github.stcksmsh.pravnik.domain.model.Unit as DocUnit
import io.github.stcksmsh.pravnik.domain.model.Collection as CollectionModel

interface DocumentsRepo {
    suspend fun listAll(): List<Document>
    suspend fun get(id: String): Document?
    suspend fun star(docId: String)
    suspend fun unstar(docId: String)
    suspend fun listStarred(): List<Document>
}

interface UnitsRepo {
    suspend fun getByAnchor(docId: String, anchor: String): DocUnit?
    suspend fun getByNumber(docId: String, number: Int): DocUnit?
    suspend fun listForLevel(docId: String, level: Int): List<DocUnit>
    suspend fun listLabels(docId: String): List<UnitLabel>
}

data class UnitLabel(val anchor: String, val label: String?, val number: Int?, val title: String?)

// Search

data class SearchFilters(
    val types: Set<DocumentType>? = null,
    val docIds: Set<String>? = null,
    val unitNumberRange: IntRange? = null,
    val court: String? = null,
    val outcome: String? = null,
    val caseYear: Int? = null,
    val authority: String? = null,
    val practiceYear: Int? = null,
)

data class SearchResult(
    val rows: List<SearchRow>,
    val facets: SearchFacets,
)

data class SearchRow(
    val unitId: Long,
    val docId: String,
    val docTitle: String,
    val docType: DocumentType,
    val unitAnchor: String,
    val unitTitle: String?,
    val snippetHtml: String,
)

data class SearchFacets(
    val typeCounts: Map<DocumentType, Int>,
    val docCounts: Map<String, Int>,
    val courts: Map<String, Int>,
    val caseYears: Map<Int, Int>,
    val authorities: Map<String, Int>,
    val practiceYears: Map<Int, Int>,
)

interface SearchRepo {
    suspend fun search(query: String, filters: SearchFilters = SearchFilters(), limit: Int = 50, offset: Int = 0): SearchResult
}

interface DefinitionsRepo { suspend fun listForDoc(docId: String): List<Definition> }
interface TariffsRepo { suspend fun listForDoc(docId: String): List<Tariff>; suspend fun getByKey(docId: String, key: String): Tariff? }
interface CaseMetaRepo { suspend fun get(docId: String): CaseMeta?; suspend fun upsert(meta: CaseMeta) }
interface PracticeMetaRepo { suspend fun get(docId: String): PracticeMeta?; suspend fun upsert(meta: PracticeMeta) }

interface CitatorRepo {
    suspend fun listFrom(docId: String): List<CitationEdge>
    suspend fun listTo(docId: String): List<CitationEdge>
    suspend fun addAll(edges: List<CitationEdge>)
}

interface BookmarksRepo {
    suspend fun listFolders(): List<BookmarkFolder>
    suspend fun upsertFolder(folder: BookmarkFolder): Long
    suspend fun deleteFolder(id: Long)
    suspend fun listBookmarks(): List<Bookmark>
    suspend fun upsertBookmark(item: Bookmark): Long
    suspend fun deleteBookmark(id: Long)
}

interface CollectionsRepo {
    suspend fun listCollections(): List<CollectionModel>
    suspend fun upsertCollection(item: CollectionModel): Long
    suspend fun deleteCollection(id: Long)
    suspend fun listItems(collectionId: Long): List<CollectionItem>
    suspend fun upsertItem(item: CollectionItem)
    suspend fun deleteItem(collectionId: Long, position: Int)
}

interface NotesRepo { suspend fun listAll(): List<Note>; suspend fun listForDoc(docId: String): List<Note>; suspend fun upsert(note: Note): Long; suspend fun delete(id: Long) }
interface HistoryRepo { suspend fun recent(limit: Int): List<History>; suspend fun insert(item: History): Long; suspend fun clear() }

interface SavedSearchesRepo { suspend fun listSaved(): List<SavedSearch>; suspend fun upsert(item: SavedSearch): Long; suspend fun delete(id: Long) }
interface TemplatesRepo { suspend fun listTemplates(): List<Template>; suspend fun upsert(item: Template): Long; suspend fun delete(id: Long) }
interface ChecklistsRepo { suspend fun listChecklists(): List<Checklist>; suspend fun upsert(item: Checklist): Long; suspend fun delete(id: Long) }
interface DocketsRepo {
    suspend fun listDockets(): List<Docket>
    suspend fun upsertDocket(item: Docket): Long
    suspend fun deleteDocket(id: Long)
    suspend fun listEvents(docketId: Long): List<DocketEvent>
    suspend fun upsertEvent(item: DocketEvent): Long
    suspend fun deleteEvent(id: Long)
}
