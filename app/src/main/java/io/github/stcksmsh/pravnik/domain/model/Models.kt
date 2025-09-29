package io.github.stcksmsh.pravnik.domain.model

import java.time.LocalDate

enum class DocumentType { CONSTITUTION, LAW, BYLAW, CASE, PRACTICE }

data class Document(
    val id: String,
    val title: String,
    val type: DocumentType,
    val language: String,
    val jurisdiction: String,
    val citation: String?,
    val promulgatedOn: LocalDate?,
    val effectiveOn: LocalDate?,
    val repealedOn: LocalDate?,
)

data class Unit(
    val id: Long,
    val docId: String,
    val level: Int,
    val label: String?,
    val number: Int?,
    val title: String?,
    val anchor: String,
    val text: String,
)

data class Definition(
    val id: Long,
    val docId: String,
    val term: String,
    val text: String,
    val unitAnchor: String?,
)

data class Tariff(
    val id: Long,
    val docId: String,
    val key: String,
    val title: String?,
    val amount: String?,
    val notes: String?,
)

data class CaseMeta(
    val docId: String,
    val court: String?,
    val caseNumber: String?,
    val decidedOn: LocalDate?,
    val ecli: String?,
    val parties: String?,
    val keywords: String?,
    val outcome: String?,
)

data class PracticeMeta(
    val docId: String,
    val authority: String?,
    val refNumber: String?,
    val issuedOn: LocalDate?,
    val category: String?,
    val keywords: String?,
)

enum class CitationRelation { cites, interprets, implements, overrules, follows }

data class CitationEdge(
    val fromDocId: String,
    val fromAnchor: String?,
    val toDocId: String,
    val toAnchor: String?,
    val relation: CitationRelation,
    val weight: Int,
    val note: String?,
)

data class CrossRefHint(
    val docId: String,
    val unitAnchor: String,
    val refText: String,
    val targetDocId: String?,
    val targetAnchor: String?,
)

data class BookmarkFolder(val id: Long, val name: String, val createdAt: Long)

data class Bookmark(
    val id: Long,
    val folderId: Long?,
    val docId: String,
    val unitAnchor: String,
    val title: String?,
    val note: String?,
    val tagsCsv: String?,
    val createdAt: Long,
)

data class Note(
    val id: Long,
    val docId: String,
    val unitAnchor: String,
    val title: String?,
    val body: String,
    val updatedAt: Long,
)

data class History(
    val id: Long,
    val docId: String,
    val unitAnchor: String,
    val visitedAt: Long,
)

data class StarredDoc(val docId: String, val starredAt: Long)

data class Collection(val id: Long, val name: String, val createdAt: Long)

data class CollectionItem(
    val collectionId: Long,
    val docId: String,
    val unitAnchor: String,
    val position: Int,
)

data class SavedSearch(
    val id: Long,
    val query: String,
    val filtersJson: String?,
    val createdAt: Long,
    val notify: Boolean,
)

data class SearchQuery(val id: Long, val query: String, val createdAt: Long)

data class Template(
    val id: Long,
    val title: String,
    val language: String,
    val bodyMd: String,
    val tagsCsv: String?,
    val createdAt: Long,
)

data class Checklist(
    val id: Long,
    val title: String,
    val docId: String?,
    val unitAnchor: String?,
    val itemsJson: String,
    val createdAt: Long,
)

data class Docket(
    val id: Long,
    val caseName: String,
    val caseNumber: String?,
    val court: String?,
    val notes: String?,
    val createdAt: Long,
)

data class DocketEvent(
    val id: Long,
    val docketId: Long,
    val title: String,
    val date: Long,
    val note: String?,
    val reminder: Long?,
)
