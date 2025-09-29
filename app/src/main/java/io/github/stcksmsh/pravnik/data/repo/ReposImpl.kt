package io.github.stcksmsh.pravnik.data.repo

import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.stcksmsh.pravnik.data.db.AppDb
import io.github.stcksmsh.pravnik.data.db.dao.SearchRow as DbSearchRow
import io.github.stcksmsh.pravnik.data.db.dao.FacetRow as DbFacetRow
import io.github.stcksmsh.pravnik.data.db.entity.*
import io.github.stcksmsh.pravnik.domain.model.*
import io.github.stcksmsh.pravnik.domain.repo.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentsRepoImpl(private val db: AppDb) : DocumentsRepo {
    override suspend fun listAll(): List<Document> = withContext(Dispatchers.IO) {
        db.documentDao().listAll().map { it.toDomain() }
    }

    override suspend fun get(id: String): Document? = withContext(Dispatchers.IO) {
        db.documentDao().get(id)?.toDomain()
    }

    override suspend fun star(docId: String) = withContext(Dispatchers.IO) {
        db.starredDao().star(StarredDocEntity(docId, System.currentTimeMillis()))
    }

    override suspend fun unstar(docId: String) = withContext(Dispatchers.IO) {
        db.starredDao().unstar(docId)
    }

    override suspend fun listStarred(): List<Document> = withContext(Dispatchers.IO) {
        db.starredDao().listStarred().map { it.toDomain() }
    }
}

class UnitsRepoImpl(private val db: AppDb) : UnitsRepo {
    override suspend fun getByAnchor(docId: String, anchor: String): io.github.stcksmsh.pravnik.domain.model.Unit? = withContext(Dispatchers.IO) {
        db.unitDao().getByAnchor(docId, anchor)?.toDomain()
    }

    override suspend fun getByNumber(docId: String, number: Int): io.github.stcksmsh.pravnik.domain.model.Unit? = withContext(Dispatchers.IO) {
        db.unitDao().getByNumber(docId, number)?.toDomain()
    }

    override suspend fun listForLevel(docId: String, level: Int): List<io.github.stcksmsh.pravnik.domain.model.Unit> = withContext(Dispatchers.IO) {
        db.unitDao().listForLevel(docId, level).map { it.toDomain() }
    }

    override suspend fun listLabels(docId: String) = withContext(Dispatchers.IO) {
        db.unitDao().listLabels(docId).map { it.toDomain() }
    }
}

class SearchRepoImpl(private val db: AppDb) : SearchRepo {
    override suspend fun search(query: String, filters: SearchFilters, limit: Int, offset: Int): SearchResult = withContext(Dispatchers.IO) {
        val sqlAndArgs = buildSearchSql(query, filters, limit, offset)
        val rows = db.searchDao().rawSearch(SimpleSQLiteQuery(sqlAndArgs.first, sqlAndArgs.second.toTypedArray()))
        val mapped = rows.map { it.toDomain() }
        val facets = computeFacets(query, filters)
        SearchResult(mapped, facets)
    }

    private suspend fun computeFacets(query: String, filters: SearchFilters): SearchFacets {
        val (whereSql, args) = buildWhereClause(query, filters)
        val fromJoins = """
            FROM units_fts
            JOIN units u ON u.id = units_fts.rowid
            JOIN documents d ON d.id = u.docId
            LEFT JOIN case_meta cm ON cm.docId = d.id
            LEFT JOIN practice_meta pm ON pm.docId = d.id
        """.trimIndent()

        val typeSql = """
            SELECT d.type AS key, COUNT(1) cnt
            $fromJoins
            $whereSql
            GROUP BY d.type
        """.trimIndent()
        val docSql = """
            SELECT d.id AS key, COUNT(1) cnt
            $fromJoins
            $whereSql
            GROUP BY d.id
        """.trimIndent()
        val courtsSql = """
            SELECT cm.court AS key, COUNT(1) cnt
            $fromJoins
            $whereSql AND d.type = 'CASE' AND cm.court IS NOT NULL AND cm.court <> ''
            GROUP BY cm.court
        """.trimIndent()
        val caseYearsSql = """
            SELECT substr(cm.decidedOn,1,4) AS key, COUNT(1) cnt
            $fromJoins
            $whereSql AND d.type = 'CASE' AND cm.decidedOn IS NOT NULL AND length(substr(cm.decidedOn,1,4)) = 4
            GROUP BY substr(cm.decidedOn,1,4)
        """.trimIndent()
        val authoritiesSql = """
            SELECT pm.authority AS key, COUNT(1) cnt
            $fromJoins
            $whereSql AND d.type = 'PRACTICE' AND pm.authority IS NOT NULL AND pm.authority <> ''
            GROUP BY pm.authority
        """.trimIndent()
        val practiceYearsSql = """
            SELECT substr(pm.issuedOn,1,4) AS key, COUNT(1) cnt
            $fromJoins
            $whereSql AND d.type = 'PRACTICE' AND pm.issuedOn IS NOT NULL AND length(substr(pm.issuedOn,1,4)) = 4
            GROUP BY substr(pm.issuedOn,1,4)
        """.trimIndent()

        val typeRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(typeSql, args.toTypedArray()))
        val docRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(docSql, args.toTypedArray()))
        val courtRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(courtsSql, args.toTypedArray()))
        val caseYearRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(caseYearsSql, args.toTypedArray()))
        val authorityRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(authoritiesSql, args.toTypedArray()))
        val practiceYearRows: List<DbFacetRow> = db.searchDao().rawFacet(SimpleSQLiteQuery(practiceYearsSql, args.toTypedArray()))

        val typeCounts = typeRows.filter { it.key.isNotEmpty() }.associate { DocumentType.valueOf(it.key) to it.cnt }
        val docCounts = docRows.associate { it.key to it.cnt }
        val courts = courtRows.associate { it.key to it.cnt }
        val caseYears = caseYearRows.mapNotNull { it.key.toIntOrNull()?.let { y -> y to it.cnt } }.toMap()
        val authorities = authorityRows.associate { it.key to it.cnt }
        val practiceYears = practiceYearRows.mapNotNull { it.key.toIntOrNull()?.let { y -> y to it.cnt } }.toMap()
        return SearchFacets(typeCounts, docCounts, courts, caseYears, authorities, practiceYears)
    }

    private fun DbSearchRow.toDomain() = io.github.stcksmsh.pravnik.domain.repo.SearchRow(
        unitId = unitId,
        docId = docId,
        docTitle = docTitle,
        docType = DocumentType.valueOf(docType),
        unitAnchor = unitAnchor,
        unitTitle = unitTitle,
        snippetHtml = snippet,
    )

    private fun buildSearchSql(query: String, filters: SearchFilters, limit: Int, offset: Int): Pair<String, List<Any>> {
        val (whereSql, args) = buildWhereClause(query, filters)
        val sql = """
            SELECT u.id AS unitId, d.id AS docId, d.title AS docTitle, d.type AS docType, u.anchor AS unitAnchor, u.title AS unitTitle,
                   snippet(units_fts, '<b>', '</b>', '…') AS snippet
            FROM units_fts JOIN units u ON u.id = units_fts.rowid JOIN documents d ON d.id = u.docId
            LEFT JOIN case_meta cm ON cm.docId = d.id
            LEFT JOIN practice_meta pm ON pm.docId = d.id
            $whereSql
            ORDER BY u.number ASC, u.id ASC
            LIMIT ? OFFSET ?
        """.trimIndent()
        val finalArgs = args.toMutableList()
        finalArgs += limit
        finalArgs += offset
        return sql to finalArgs
    }

    private fun buildWhereClause(query: String, filters: SearchFilters): Pair<String, List<Any>> {
        val where = StringBuilder("WHERE units_fts MATCH ?")
        val args = mutableListOf<Any>(query)
        if (!filters.docIds.isNullOrEmpty()) {
            where.append(" AND d.id IN (${filters.docIds.joinToString(",") { "?" }})")
            args.addAll(filters.docIds)
        }
        if (!filters.types.isNullOrEmpty()) {
            where.append(" AND d.type IN (${filters.types.joinToString(",") { "?" }})")
            args.addAll(filters.types.map { it.name })
        }
        filters.unitNumberRange?.let {
            where.append(" AND u.number BETWEEN ? AND ?")
            args.add(it.first)
            args.add(it.last)
        }
        filters.court?.let { where.append(" AND cm.court = ?"); args.add(it) }
        filters.outcome?.let { where.append(" AND cm.outcome = ?"); args.add(it) }
        filters.caseYear?.let { where.append(" AND substr(cm.decidedOn,1,4) = ?"); args.add(String.format("%04d", it)) }
        filters.authority?.let { where.append(" AND pm.authority = ?"); args.add(it) }
        filters.practiceYear?.let { where.append(" AND substr(pm.issuedOn,1,4) = ?"); args.add(String.format("%04d", it)) }
        return where.toString() to args
    }
}


class DefinitionsRepoImpl(private val db: AppDb) : DefinitionsRepo {
    override suspend fun listForDoc(docId: String) = withContext(Dispatchers.IO) {
        db.definitionDao().listForDoc(docId).map { it.toDomain() }
    }
}

class TariffsRepoImpl(private val db: AppDb) : TariffsRepo {
    override suspend fun listForDoc(docId: String) = withContext(Dispatchers.IO) {
        db.tariffDao().listForDoc(docId).map { it.toDomain() }
    }

    override suspend fun getByKey(docId: String, key: String) = withContext(Dispatchers.IO) {
        db.tariffDao().getByKey(docId, key)?.toDomain()
    }
}

class CaseMetaRepoImpl(private val db: AppDb) : CaseMetaRepo {
    override suspend fun get(docId: String) = withContext(Dispatchers.IO) {
        db.caseMetaDao().get(docId)?.toDomain()
    }

    override suspend fun upsert(meta: CaseMeta) = withContext(Dispatchers.IO) {
        db.caseMetaDao().upsert(meta.toEntity())
    }
}

class PracticeMetaRepoImpl(private val db: AppDb) : PracticeMetaRepo {
    override suspend fun get(docId: String) = withContext(Dispatchers.IO) {
        db.practiceMetaDao().get(docId)?.toDomain()
    }

    override suspend fun upsert(meta: PracticeMeta) = withContext(Dispatchers.IO) {
        db.practiceMetaDao().upsert(meta.toEntity())
    }
}

class CitatorRepoImpl(private val db: AppDb) : CitatorRepo {
    override suspend fun listFrom(docId: String) = withContext(Dispatchers.IO) {
        db.citationDao().listFrom(docId).map { it.toDomain() }
    }

    override suspend fun listTo(docId: String) = withContext(Dispatchers.IO) {
        db.citationDao().listTo(docId).map { it.toDomain() }
    }

    override suspend fun addAll(edges: List<CitationEdge>) = withContext(Dispatchers.IO) {
        db.citationDao().insertAll(edges.map { it.toEntity() })
    }
}

class BookmarksRepoImpl(private val db: AppDb) : BookmarksRepo {
    override suspend fun listFolders() = withContext(Dispatchers.IO) {
        db.bookmarkDao().listFolders().map { it.toDomain() }
    }

    override suspend fun upsertFolder(folder: BookmarkFolder) = withContext(Dispatchers.IO) {
        db.bookmarkDao().upsertFolder(folder.toEntity())
    }

    override suspend fun deleteFolder(id: Long) = withContext(Dispatchers.IO) {
        db.bookmarkDao().deleteFolder(id)
    }

    override suspend fun listBookmarks() = withContext(Dispatchers.IO) {
        db.bookmarkDao().listBookmarks().map { it.toDomain() }
    }

    override suspend fun upsertBookmark(item: Bookmark) = withContext(Dispatchers.IO) {
        db.bookmarkDao().upsertBookmark(item.toEntity())
    }

    override suspend fun deleteBookmark(id: Long) = withContext(Dispatchers.IO) {
        db.bookmarkDao().deleteBookmark(id)
    }
}

class CollectionsRepoImpl(private val db: AppDb) : CollectionsRepo {
    override suspend fun listCollections(): List<io.github.stcksmsh.pravnik.domain.model.Collection> = withContext(Dispatchers.IO) {
        db.collectionsDao().listCollections().map { it.toDomain() }
    }

    override suspend fun upsertCollection(item: io.github.stcksmsh.pravnik.domain.model.Collection): Long = withContext(Dispatchers.IO) {
        db.collectionsDao().upsertCollection(item.toEntity())
    }

    override suspend fun deleteCollection(id: Long) = withContext(Dispatchers.IO) {
        db.collectionsDao().deleteCollection(id)
    }

    override suspend fun listItems(collectionId: Long) = withContext(Dispatchers.IO) {
        db.collectionsDao().listItems(collectionId).map { it.toDomain() }
    }

    override suspend fun upsertItem(item: CollectionItem) = withContext(Dispatchers.IO) {
        db.collectionsDao().upsertItem(item.toEntity())
    }

    override suspend fun deleteItem(collectionId: Long, position: Int) = withContext(Dispatchers.IO) {
        db.collectionsDao().deleteItem(collectionId, position)
    }
}

class NotesRepoImpl(private val db: AppDb) : NotesRepo {
    override suspend fun listAll() = withContext(Dispatchers.IO) {
        db.noteDao().listAll().map { it.toDomain() }
    }
    override suspend fun listForDoc(docId: String) = withContext(Dispatchers.IO) {
        db.noteDao().listForDoc(docId).map { it.toDomain() }
    }
    override suspend fun upsert(note: Note) = withContext(Dispatchers.IO) {
        db.noteDao().upsert(note.toEntity())
    }
    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.noteDao().delete(id)
    }
}

class HistoryRepoImpl(private val db: AppDb) : HistoryRepo {
    override suspend fun recent(limit: Int) = withContext(Dispatchers.IO) {
        db.historyDao().recent(limit).map { it.toDomain() }
    }

    override suspend fun insert(item: History) = withContext(Dispatchers.IO) {
        db.historyDao().insert(item.toEntity())
    }

    override suspend fun clear() = withContext(Dispatchers.IO) { db.historyDao().clear() }
}

class SavedSearchesRepoImpl(private val db: AppDb) : SavedSearchesRepo {
    override suspend fun listSaved() = withContext(Dispatchers.IO) {
        db.savedSearchDao().listSaved().map { it.toDomain() }
    }

    override suspend fun upsert(item: SavedSearch) = withContext(Dispatchers.IO) {
        db.savedSearchDao().upsert(item.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.savedSearchDao().delete(id)
    }
}

class TemplatesRepoImpl(private val db: AppDb) : TemplatesRepo {
    override suspend fun listTemplates() = withContext(Dispatchers.IO) {
        db.templatesDao().listTemplates().map { it.toDomain() }
    }

    override suspend fun upsert(item: Template) = withContext(Dispatchers.IO) {
        db.templatesDao().upsert(item.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.templatesDao().delete(id)
    }
}

class ChecklistsRepoImpl(private val db: AppDb) : ChecklistsRepo {
    override suspend fun listChecklists() = withContext(Dispatchers.IO) {
        db.checklistsDao().listChecklists().map { it.toDomain() }
    }

    override suspend fun upsert(item: Checklist) = withContext(Dispatchers.IO) {
        db.checklistsDao().upsert(item.toEntity())
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        db.checklistsDao().delete(id)
    }
}

class DocketsRepoImpl(private val db: AppDb) : DocketsRepo {
    override suspend fun listDockets() = withContext(Dispatchers.IO) {
        db.docketsDao().listDockets().map { it.toDomain() }
    }

    override suspend fun upsertDocket(item: Docket) = withContext(Dispatchers.IO) {
        db.docketsDao().upsertDocket(item.toEntity())
    }

    override suspend fun deleteDocket(id: Long) = withContext(Dispatchers.IO) {
        db.docketsDao().deleteDocket(id)
    }

    override suspend fun listEvents(docketId: Long) = withContext(Dispatchers.IO) {
        db.docketsDao().listEvents(docketId).map { it.toDomain() }
    }

    override suspend fun upsertEvent(item: DocketEvent) = withContext(Dispatchers.IO) {
        db.docketsDao().upsertEvent(item.toEntity())
    }

    override suspend fun deleteEvent(id: Long) = withContext(Dispatchers.IO) {
        db.docketsDao().deleteEvent(id)
    }
}
