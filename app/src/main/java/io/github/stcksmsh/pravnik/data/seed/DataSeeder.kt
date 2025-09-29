package io.github.stcksmsh.pravnik.data.seed

import io.github.stcksmsh.pravnik.data.db.AppDb
import io.github.stcksmsh.pravnik.data.repo.toEntity
import io.github.stcksmsh.pravnik.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class DataSeeder(private val db: AppDb) {
    suspend fun seedIfEmpty() = withContext(Dispatchers.IO) {
        if (db.documentDao().listAll().isNotEmpty()) return@withContext

        // Documents
        val law = Document(
            id = "law.sample",
            title = "Zakon o Primjeru",
            type = DocumentType.LAW,
            language = "sr-Latn",
            jurisdiction = "ME",
            citation = "Sl. list ME 1/2025",
            promulgatedOn = LocalDate.of(2025, 1, 15),
            effectiveOn = LocalDate.of(2025, 2, 1),
            repealedOn = null,
        )
        val caseDoc = Document(
            id = "case.sample",
            title = "Vrhovni sud, Pž 123/2024",
            type = DocumentType.CASE,
            language = "sr-Latn",
            jurisdiction = "ME",
            citation = "Pž 123/2024",
            promulgatedOn = null,
            effectiveOn = null,
            repealedOn = null,
        )
        val practiceDoc = Document(
            id = "practice.sample",
            title = "Uputstvo Ministarstva Finansija 02-123/2024",
            type = DocumentType.PRACTICE,
            language = "sr-Latn",
            jurisdiction = "ME",
            citation = "02-123/2024",
            promulgatedOn = null,
            effectiveOn = null,
            repealedOn = null,
        )

        db.documentDao().upsertAll(listOf(law.toEntity(), caseDoc.toEntity(), practiceDoc.toEntity()))

        // Units for law (two articles)
        val u1 = Unit(
            id = 0,
            docId = law.id,
            level = 2,
            label = "Član 1",
            number = 1,
            title = "Predmet zakona",
            anchor = "clan-1",
            text = """
                (1) Ovim zakonom uređuje se primjer radi.
                – Prva alineja sa primjerom.
                1) Prva tačka primera.
                2) Druga tačka primera.
                Vidi čl. 2 za nastavak.
            """.trimIndent()
        )
        val u2 = Unit(
            id = 0,
            docId = law.id,
            level = 2,
            label = "Član 2",
            number = 2,
            title = "Primjena",
            anchor = "clan-2",
            text = """
                (1) Odredbe ovog zakona primjenjuju se od dana stupanja na snagu.
                – Alineja vezana za rokove.
                1) Tačka o izuzecima.
            """.trimIndent()
        )
        db.unitDao().upsertAll(listOf(u1.toEntity(), u2.toEntity()))

        // Definitions and Tariffs for law
        val def1 = Definition(id = 0, docId = law.id, term = "pojam", text = "Značenje pojma u kontekstu zakona.", unitAnchor = "clan-1")
        val def2 = Definition(id = 0, docId = law.id, term = "definicija", text = "Definicija termina korišćenih u zakonu.", unitAnchor = "clan-2")
        db.definitionDao().upsertAll(listOf(def1.toEntity(), def2.toEntity()))

        val t1 = Tariff(id = 0, docId = law.id, key = "Tarifni broj 1", title = "Taksa A", amount = "10 EUR", notes = null)
        val t2 = Tariff(id = 0, docId = law.id, key = "Tarifni broj 2", title = "Taksa B", amount = "20 EUR", notes = null)
        db.tariffDao().upsertAll(listOf(t1.toEntity(), t2.toEntity()))

        // Case meta and unit
        val caseMeta = CaseMeta(docId = caseDoc.id, court = "Vrhovni sud", caseNumber = "Pž 123/2024", decidedOn = LocalDate.of(2024, 12, 20), ecli = null, parties = null, keywords = "zakon; tumačenje", outcome = "usvojena žalba")
        db.caseMetaDao().upsert(caseMeta.toEntity())
        val cu = Unit(id = 0, docId = caseDoc.id, level = 2, label = "Obrazloženje", number = null, title = "Obrazloženje", anchor = "body", text = "Sud se poziva na čl. 1 Zakona o Primjeru.")
        db.unitDao().upsertAll(listOf(cu.toEntity()))

        // Practice meta and unit
        val practiceMeta = PracticeMeta(docId = practiceDoc.id, authority = "Ministarstvo finansija", refNumber = "02-123/2024", issuedOn = LocalDate.of(2024, 11, 10), category = "uputstvo", keywords = "takse; primjena")
        db.practiceMetaDao().upsert(practiceMeta.toEntity())
        val pu = Unit(id = 0, docId = practiceDoc.id, level = 2, label = "Tumačenje", number = null, title = "Tumačenje primjene taksi", anchor = "body", text = "Ovim uputstvom se pojašnjava tumačenje odredbi čl. 2 Zakona o Primjeru.")
        db.unitDao().upsertAll(listOf(pu.toEntity()))

        // Citation: case cites law čl.1
        val edge = CitationEdge(fromDocId = caseDoc.id, fromAnchor = "body", toDocId = law.id, toAnchor = "clan-1", relation = CitationRelation.cites, weight = 1, note = null)
        db.citationDao().insertAll(listOf(edge.toEntity()))

        // Star some docs
        db.starredDao().star(StarredDocEntity(law.id, System.currentTimeMillis()))
        db.starredDao().star(StarredDocEntity(caseDoc.id, System.currentTimeMillis() - 1000))

        // Bookmarks
        val now = System.currentTimeMillis()
        val bm1 = Bookmark(id = 0, folderId = null, docId = law.id, unitAnchor = u1.anchor, title = "Član 1", note = null, tagsCsv = "intro;primjer", createdAt = now - 86400000)
        val bm2 = Bookmark(id = 0, folderId = null, docId = caseDoc.id, unitAnchor = cu.anchor, title = "Obrazloženje", note = null, tagsCsv = null, createdAt = now - 43200000)
        db.bookmarkDao().upsertBookmark(bm1.toEntity())
        db.bookmarkDao().upsertBookmark(bm2.toEntity())

        // History
        val h1 = History(id = 0, docId = law.id, unitAnchor = u2.anchor, visitedAt = now - 600000)
        val h2 = History(id = 0, docId = practiceDoc.id, unitAnchor = pu.anchor, visitedAt = now - 300000)
        db.historyDao().insert(h1.toEntity())
        db.historyDao().insert(h2.toEntity())

        // Saved searches
        val s1 = SavedSearch(id = 0, query = "taksa", filtersJson = null, createdAt = now - 7200000, notify = false)
        val s2 = SavedSearch(id = 0, query = "Vrhovni sud", filtersJson = null, createdAt = now - 14400000, notify = true)
        db.savedSearchDao().upsert(s1.toEntity())
        db.savedSearchDao().upsert(s2.toEntity())

        // Recent queries
        val q1 = SearchQuery(id = 0, query = "član 1", createdAt = now - 200000)
        val q2 = SearchQuery(id = 0, query = "tumačenje", createdAt = now - 100000)
        db.searchQueryDao().insert(q1.toEntity())
        db.searchQueryDao().insert(q2.toEntity())
    }
}
