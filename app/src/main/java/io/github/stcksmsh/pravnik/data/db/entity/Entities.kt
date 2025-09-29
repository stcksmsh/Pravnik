package io.github.stcksmsh.pravnik.data.db.entity

import androidx.room.*

@Entity(
    tableName = "documents",
    indices = [Index(value = ["type"])]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val language: String,
    val jurisdiction: String,
    val citation: String?,
    val promulgatedOn: String?,
    val effectiveOn: String?,
    val repealedOn: String?,
)

@Entity(
    tableName = "units",
    indices = [
        Index(value = ["docId", "anchor"], unique = true),
        Index(value = ["number"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val level: Int,
    val label: String?,
    val number: Int?,
    val title: String?,
    val anchor: String,
    val text: String,
)

@Fts4(
    contentEntity = UnitEntity::class,
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    tokenizerArgs = ["remove_diacritics=2"]
)
@Entity(tableName = "units_fts")
data class UnitFtsEntity(
    @PrimaryKey @ColumnInfo(name = "rowid") val rowId: Long,
    val title: String?,
    val text: String,
)

@Entity(
    tableName = "definitions",
    indices = [Index(value = ["term"]), Index(value = ["docId"])],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class DefinitionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val term: String,
    val text: String,
    val unitAnchor: String?,
)

@Entity(
    tableName = "tariffs",
    indices = [Index(value = ["docId"])],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class TariffEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val key: String,
    val title: String?,
    val amount: String?,
    val notes: String?,
)

@Entity(
    tableName = "case_meta",
    indices = [Index(value = ["court", "decidedOn"])],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CaseMetaEntity(
    @PrimaryKey val docId: String,
    val court: String?,
    val caseNumber: String?,
    val decidedOn: String?,
    val ecli: String?,
    val parties: String?,
    val keywords: String?,
    val outcome: String?,
)

@Entity(
    tableName = "practice_meta",
    indices = [Index(value = ["authority", "issuedOn"])],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PracticeMetaEntity(
    @PrimaryKey val docId: String,
    val authority: String?,
    val refNumber: String?,
    val issuedOn: String?,
    val category: String?,
    val keywords: String?,
)

@Entity(
    tableName = "citation_edges",
    indices = [Index(value = ["fromDocId"]), Index(value = ["toDocId"])],
    foreignKeys = [
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["fromDocId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["toDocId"], onDelete = ForeignKey.CASCADE, onUpdate = ForeignKey.CASCADE),
    ]
)
data class CitationEdgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromDocId: String,
    val fromAnchor: String?,
    val toDocId: String,
    val toAnchor: String?,
    val relation: String,
    val weight: Int,
    val note: String?,
)

@Entity(
    tableName = "crossref_hints",
    indices = [Index(value = ["docId", "unitAnchor"])],
    foreignKeys = [
        ForeignKey(
            entity = DocumentEntity::class,
            parentColumns = ["id"],
            childColumns = ["docId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CrossRefHintEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val unitAnchor: String,
    val refText: String,
    val targetDocId: String?,
    val targetAnchor: String?,
)

@Entity(tableName = "bookmark_folders")
data class BookmarkFolderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["folderId"]), Index(value = ["docId"])],
    foreignKeys = [
        ForeignKey(entity = BookmarkFolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["docId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderId: Long?,
    val docId: String,
    val unitAnchor: String,
    val title: String?,
    val note: String?,
    val tagsCsv: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "notes",
    indices = [Index(value = ["docId", "unitAnchor"])],
    foreignKeys = [
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["docId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val unitAnchor: String,
    val title: String?,
    val body: String,
    val updatedAt: Long,
)

@Entity(
    tableName = "history",
    indices = [Index(value = ["visitedAt"]), Index(value = ["docId"])],
    foreignKeys = [
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["docId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docId: String,
    val unitAnchor: String,
    val visitedAt: Long,
)

@Entity(tableName = "starred_docs")
data class StarredDocEntity(
    @PrimaryKey val docId: String,
    val starredAt: Long,
)

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(
    tableName = "collection_items",
    primaryKeys = ["collectionId", "position"],
    indices = [Index(value = ["docId"])],
    foreignKeys = [
        ForeignKey(entity = CollectionEntity::class, parentColumns = ["id"], childColumns = ["collectionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = DocumentEntity::class, parentColumns = ["id"], childColumns = ["docId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class CollectionItemEntity(
    val collectionId: Long,
    val docId: String,
    val unitAnchor: String,
    val position: Int,
)

@Entity(tableName = "saved_searches")
data class SavedSearchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val filtersJson: String?,
    val createdAt: Long,
    val notify: Boolean,
)

@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val createdAt: Long,
)

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val language: String,
    val bodyMd: String,
    val tagsCsv: String?,
    val createdAt: Long,
)

@Entity(tableName = "checklists")
data class ChecklistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val docId: String?,
    val unitAnchor: String?,
    val itemsJson: String,
    val createdAt: Long,
)

@Entity(tableName = "dockets")
data class DocketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseName: String,
    val caseNumber: String?,
    val court: String?,
    val notes: String?,
    val createdAt: Long,
)

@Entity(
    tableName = "docket_events",
    indices = [Index(value = ["docketId"])],
    foreignKeys = [
        ForeignKey(entity = DocketEntity::class, parentColumns = ["id"], childColumns = ["docketId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class DocketEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val docketId: Long,
    val title: String,
    val date: Long,
    val note: String?,
    val reminder: Long?,
)
