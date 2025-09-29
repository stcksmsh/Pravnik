package io.github.stcksmsh.pravnik.data.repo

import io.github.stcksmsh.pravnik.data.db.dao.UnitLabelRow
import io.github.stcksmsh.pravnik.data.db.entity.*
import io.github.stcksmsh.pravnik.domain.model.*
import io.github.stcksmsh.pravnik.domain.model.Unit as DocUnit
import io.github.stcksmsh.pravnik.domain.model.Collection as CollectionModel
import io.github.stcksmsh.pravnik.domain.repo.UnitLabel

private fun String.toDocType(): DocumentType = try { DocumentType.valueOf(this) } catch (_: Exception) { DocumentType.LAW }
private fun DocumentType.toStr(): String = name

private fun String?.toLocalDateOrNull(): java.time.LocalDate? = try { this?.let { java.time.LocalDate.parse(it) } } catch (_: Exception) { null }
private fun java.time.LocalDate?.toDb(): String? = this?.toString()

fun DocumentEntity.toDomain() = Document(
    id = id,
    title = title,
    type = type.toDocType(),
    language = language,
    jurisdiction = jurisdiction,
    citation = citation,
    promulgatedOn = promulgatedOn.toLocalDateOrNull(),
    effectiveOn = effectiveOn.toLocalDateOrNull(),
    repealedOn = repealedOn.toLocalDateOrNull(),
)

fun UnitEntity.toDomain() = DocUnit(
    id = id,
    docId = docId,
    level = level,
    label = label,
    number = number,
    title = title,
    anchor = anchor,
    text = text,
)

fun DefinitionEntity.toDomain() = Definition(id, docId, term, text, unitAnchor)
fun TariffEntity.toDomain() = Tariff(id, docId, key, title, amount, notes)
fun CaseMetaEntity.toDomain() = CaseMeta(docId, court, caseNumber, decidedOn.toLocalDateOrNull(), ecli, parties, keywords, outcome)
fun PracticeMetaEntity.toDomain() = PracticeMeta(docId, authority, refNumber, issuedOn.toLocalDateOrNull(), category, keywords)
fun CitationEdgeEntity.toDomain() = CitationEdge(fromDocId, fromAnchor, toDocId, toAnchor, CitationRelation.valueOf(relation), weight, note)
fun CrossRefHintEntity.toDomain() = CrossRefHint(docId, unitAnchor, refText, targetDocId, targetAnchor)
fun BookmarkFolderEntity.toDomain() = BookmarkFolder(id, name, createdAt)
fun BookmarkEntity.toDomain() = Bookmark(id, folderId, docId, unitAnchor, title, note, tagsCsv, createdAt)
fun NoteEntity.toDomain() = Note(id, docId, unitAnchor, title, body, updatedAt)
fun HistoryEntity.toDomain() = History(id, docId, unitAnchor, visitedAt)
fun StarredDocEntity.toDomain() = StarredDoc(docId, starredAt)
fun CollectionEntity.toDomain() = CollectionModel(id, name, createdAt)
fun CollectionItemEntity.toDomain() = CollectionItem(collectionId, docId, unitAnchor, position)
fun SavedSearchEntity.toDomain() = SavedSearch(id, query, filtersJson, createdAt, notify)
fun SearchQueryEntity.toDomain() = SearchQuery(id, query, createdAt)
fun TemplateEntity.toDomain() = Template(id, title, language, bodyMd, tagsCsv, createdAt)
fun ChecklistEntity.toDomain() = Checklist(id, title, docId, unitAnchor, itemsJson, createdAt)
fun DocketEntity.toDomain() = Docket(id, caseName, caseNumber, court, notes, createdAt)
fun DocketEventEntity.toDomain() = DocketEvent(id, docketId, title, date, note, reminder)

fun UnitLabelRow.toDomain() = UnitLabel(anchor, label, number, title)

// Reverse mappings for upserts where needed
fun Document.toEntity() = DocumentEntity(id, title, type.toStr(), language, jurisdiction, citation, promulgatedOn.toDb(), effectiveOn.toDb(), repealedOn.toDb())
fun DocUnit.toEntity() = UnitEntity(id, docId, level, label, number, title, anchor, text)
fun Definition.toEntity() = DefinitionEntity(id, docId, term, text, unitAnchor)
fun Tariff.toEntity() = TariffEntity(id, docId, key, title, amount, notes)
fun CaseMeta.toEntity() = CaseMetaEntity(docId, court, caseNumber, decidedOn.toDb(), ecli, parties, keywords, outcome)
fun PracticeMeta.toEntity() = PracticeMetaEntity(docId, authority, refNumber, issuedOn.toDb(), category, keywords)
fun CitationEdge.toEntity() = CitationEdgeEntity(0, fromDocId, fromAnchor, toDocId, toAnchor, relation.name, weight, note)
fun CrossRefHint.toEntity() = CrossRefHintEntity(0, docId, unitAnchor, refText, targetDocId, targetAnchor)
fun BookmarkFolder.toEntity() = BookmarkFolderEntity(id, name, createdAt)
fun Bookmark.toEntity() = BookmarkEntity(id, folderId, docId, unitAnchor, title, note, tagsCsv, createdAt)
fun Note.toEntity() = NoteEntity(id, docId, unitAnchor, title, body, updatedAt)
fun History.toEntity() = HistoryEntity(id, docId, unitAnchor, visitedAt)
fun StarredDoc.toEntity() = StarredDocEntity(docId, starredAt)
fun CollectionModel.toEntity() = CollectionEntity(id, name, createdAt)
fun CollectionItem.toEntity() = CollectionItemEntity(collectionId, docId, unitAnchor, position)
fun SavedSearch.toEntity() = SavedSearchEntity(id, query, filtersJson, createdAt, notify)
fun SearchQuery.toEntity() = SearchQueryEntity(id, query, createdAt)
fun Template.toEntity() = TemplateEntity(id, title, language, bodyMd, tagsCsv, createdAt)
fun Checklist.toEntity() = ChecklistEntity(id, title, docId, unitAnchor, itemsJson, createdAt)
fun Docket.toEntity() = DocketEntity(id, caseName, caseNumber, court, notes, createdAt)
fun DocketEvent.toEntity() = DocketEventEntity(id, docketId, title, date, note, reminder)
