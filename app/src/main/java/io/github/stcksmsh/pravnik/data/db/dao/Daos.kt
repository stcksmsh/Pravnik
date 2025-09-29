package io.github.stcksmsh.pravnik.data.db.dao

import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.stcksmsh.pravnik.data.db.entity.*

// Documents
@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY title")
    suspend fun listAll(): List<DocumentEntity>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun get(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(docs: List<DocumentEntity>)

    @Query("DELETE FROM documents")
    suspend fun clear()
}

// Units + FTS
@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE docId = :docId AND anchor = :anchor LIMIT 1")
    suspend fun getByAnchor(docId: String, anchor: String): UnitEntity?

    @Query("SELECT * FROM units WHERE docId = :docId AND number = :number LIMIT 1")
    suspend fun getByNumber(docId: String, number: Int): UnitEntity?

    @Query("SELECT * FROM units WHERE docId = :docId AND level = :level ORDER BY number, id")
    suspend fun listForLevel(docId: String, level: Int): List<UnitEntity>

    @Query("SELECT anchor, label, number, title FROM units WHERE docId = :docId ORDER BY level, number")
    suspend fun listLabels(docId: String): List<UnitLabelRow>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(units: List<UnitEntity>)

    @Query("DELETE FROM units WHERE docId = :docId")
    suspend fun clearForDoc(docId: String)
}

// Helper row for labels
data class UnitLabelRow(
    val anchor: String,
    val label: String?,
    val number: Int?,
    val title: String?,
)

// Search using raw query for flexible filters and snippets
@Dao
interface SearchDao {
    @RawQuery(observedEntities = [UnitEntity::class, UnitFtsEntity::class, DocumentEntity::class])
    suspend fun rawSearch(query: SupportSQLiteQuery): List<SearchRow>

    @RawQuery(observedEntities = [UnitEntity::class, UnitFtsEntity::class, DocumentEntity::class])
    suspend fun rawFacet(query: SupportSQLiteQuery): List<FacetRow>
}

data class SearchRow(
    val unitId: Long,
    val docId: String,
    val docTitle: String,
    val docType: String,
    val unitAnchor: String,
    val unitTitle: String?,
    val snippet: String,
)

data class FacetRow(
    val key: String,
    val cnt: Int,
)

// Definitions
@Dao
interface DefinitionDao {
    @Query("SELECT * FROM definitions WHERE docId = :docId ORDER BY term")
    suspend fun listForDoc(docId: String): List<DefinitionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DefinitionEntity>)

    @Query("DELETE FROM definitions WHERE docId = :docId")
    suspend fun clearForDoc(docId: String)
}

// Tariffs
@Dao
interface TariffDao {
    @Query("SELECT * FROM tariffs WHERE docId = :docId ORDER BY id")
    suspend fun listForDoc(docId: String): List<TariffEntity>

    @Query("SELECT * FROM tariffs WHERE docId = :docId AND `key` = :key LIMIT 1")
    suspend fun getByKey(docId: String, key: String): TariffEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<TariffEntity>)

    @Query("DELETE FROM tariffs WHERE docId = :docId")
    suspend fun clearForDoc(docId: String)
}

// Case & Practice Meta
@Dao
interface CaseMetaDao {
    @Query("SELECT * FROM case_meta WHERE docId = :docId")
    suspend fun get(docId: String): CaseMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: CaseMetaEntity)
}

@Dao
interface PracticeMetaDao {
    @Query("SELECT * FROM practice_meta WHERE docId = :docId")
    suspend fun get(docId: String): PracticeMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: PracticeMetaEntity)
}

// Citations and cross refs
@Dao
interface CitationDao {
    @Query("SELECT * FROM citation_edges WHERE fromDocId = :docId ORDER BY weight DESC, id DESC")
    suspend fun listFrom(docId: String): List<CitationEdgeEntity>

    @Query("SELECT * FROM citation_edges WHERE toDocId = :docId ORDER BY weight DESC, id DESC")
    suspend fun listTo(docId: String): List<CitationEdgeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(edges: List<CitationEdgeEntity>)

    @Query("DELETE FROM citation_edges WHERE fromDocId = :docId OR toDocId = :docId")
    suspend fun clearForDoc(docId: String)
}

@Dao
interface CrossRefHintDao {
    @Query("SELECT * FROM crossref_hints WHERE docId = :docId AND unitAnchor = :anchor")
    suspend fun listForUnit(docId: String, anchor: String): List<CrossRefHintEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<CrossRefHintEntity>)

    @Query("DELETE FROM crossref_hints WHERE docId = :docId")
    suspend fun clearForDoc(docId: String)
}

// User data: bookmarks, notes, history, starred
@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmark_folders ORDER BY createdAt DESC")
    suspend fun listFolders(): List<BookmarkFolderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(folder: BookmarkFolderEntity): Long

    @Query("DELETE FROM bookmark_folders WHERE id = :id")
    suspend fun deleteFolder(id: Long)

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    suspend fun listBookmarks(): List<BookmarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(item: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmark(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    suspend fun listAll(): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE docId = :docId ORDER BY updatedAt DESC")
    suspend fun listForDoc(docId: String): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: NoteEntity): Long

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY visitedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryEntity): Long

    @Query("DELETE FROM history")
    suspend fun clear()
}

@Dao
interface StarredDao {
    @Query("SELECT d.* FROM documents d INNER JOIN starred_docs s ON d.id = s.docId ORDER BY s.starredAt DESC")
    suspend fun listStarred(): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun star(item: StarredDocEntity)

    @Query("DELETE FROM starred_docs WHERE docId = :docId")
    suspend fun unstar(docId: String)
}

// Collections
@Dao
interface CollectionsDao {
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    suspend fun listCollections(): List<CollectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCollection(item: CollectionEntity): Long

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteCollection(id: Long)

    @Query("SELECT * FROM collection_items WHERE collectionId = :collectionId ORDER BY position ASC")
    suspend fun listItems(collectionId: Long): List<CollectionItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItem(item: CollectionItemEntity)

    @Query("DELETE FROM collection_items WHERE collectionId = :collectionId AND position = :position")
    suspend fun deleteItem(collectionId: Long, position: Int)
}

// Saved searches and queries
@Dao
interface SavedSearchDao {
    @Query("SELECT * FROM saved_searches ORDER BY createdAt DESC")
    suspend fun listSaved(): List<SavedSearchEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: SavedSearchEntity): Long

    @Query("DELETE FROM saved_searches WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface SearchQueryDao {
    @Query("SELECT * FROM search_queries ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<SearchQueryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SearchQueryEntity): Long
}

// Templates & Checklists
@Dao
interface TemplatesDao {
    @Query("SELECT * FROM templates ORDER BY createdAt DESC")
    suspend fun listTemplates(): List<TemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: TemplateEntity): Long

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface ChecklistsDao {
    @Query("SELECT * FROM checklists ORDER BY createdAt DESC")
    suspend fun listChecklists(): List<ChecklistEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ChecklistEntity): Long

    @Query("DELETE FROM checklists WHERE id = :id")
    suspend fun delete(id: Long)
}

// Dockets
@Dao
interface DocketsDao {
    @Query("SELECT * FROM dockets ORDER BY createdAt DESC")
    suspend fun listDockets(): List<DocketEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocket(item: DocketEntity): Long

    @Query("DELETE FROM dockets WHERE id = :id")
    suspend fun deleteDocket(id: Long)

    @Query("SELECT * FROM docket_events WHERE docketId = :docketId ORDER BY date ASC")
    suspend fun listEvents(docketId: Long): List<DocketEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(item: DocketEventEntity): Long

    @Query("DELETE FROM docket_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)
}
