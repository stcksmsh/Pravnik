package io.github.stcksmsh.pravnik.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.stcksmsh.pravnik.data.db.dao.*
import io.github.stcksmsh.pravnik.data.db.entity.*

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        DocumentEntity::class,
        UnitEntity::class,
        UnitFtsEntity::class,
        DefinitionEntity::class,
        TariffEntity::class,
        CaseMetaEntity::class,
        PracticeMetaEntity::class,
        CitationEdgeEntity::class,
        CrossRefHintEntity::class,
        BookmarkFolderEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        HistoryEntity::class,
        StarredDocEntity::class,
        CollectionEntity::class,
        CollectionItemEntity::class,
        SavedSearchEntity::class,
        SearchQueryEntity::class,
        TemplateEntity::class,
        ChecklistEntity::class,
        DocketEntity::class,
        DocketEventEntity::class,
    ]
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun unitDao(): UnitDao
    abstract fun searchDao(): SearchDao
    abstract fun definitionDao(): DefinitionDao
    abstract fun tariffDao(): TariffDao
    abstract fun caseMetaDao(): CaseMetaDao
    abstract fun practiceMetaDao(): PracticeMetaDao
    abstract fun citationDao(): CitationDao
    abstract fun crossRefHintDao(): CrossRefHintDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun historyDao(): HistoryDao
    abstract fun starredDao(): StarredDao
    abstract fun collectionsDao(): CollectionsDao
    abstract fun savedSearchDao(): SavedSearchDao
    abstract fun searchQueryDao(): SearchQueryDao
    abstract fun templatesDao(): TemplatesDao
    abstract fun checklistsDao(): ChecklistsDao
    abstract fun docketsDao(): DocketsDao
}
