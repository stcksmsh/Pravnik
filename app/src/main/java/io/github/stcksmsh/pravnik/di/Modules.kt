package io.github.stcksmsh.pravnik.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.stcksmsh.pravnik.data.db.AppDb
import io.github.stcksmsh.pravnik.data.repo.*
import io.github.stcksmsh.pravnik.domain.repo.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Modules {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext context: Context): AppDb = Room.databaseBuilder(
        context,
        AppDb::class.java,
        "pravnik.db"
    )
        .addMigrations(*io.github.stcksmsh.pravnik.data.db.migrations.AppDbMigrations.ALL)
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides @Singleton fun provideDocumentsRepo(db: AppDb): DocumentsRepo = DocumentsRepoImpl(db)
    @Provides @Singleton fun provideUnitsRepo(db: AppDb): UnitsRepo = UnitsRepoImpl(db)
    @Provides @Singleton fun provideSearchRepo(db: AppDb): SearchRepo = SearchRepoImpl(db)
    @Provides @Singleton fun provideDefinitionsRepo(db: AppDb): DefinitionsRepo = DefinitionsRepoImpl(db)
    @Provides @Singleton fun provideTariffsRepo(db: AppDb): TariffsRepo = TariffsRepoImpl(db)
    @Provides @Singleton fun provideCaseMetaRepo(db: AppDb): CaseMetaRepo = CaseMetaRepoImpl(db)
    @Provides @Singleton fun providePracticeMetaRepo(db: AppDb): PracticeMetaRepo = PracticeMetaRepoImpl(db)
    @Provides @Singleton fun provideCitatorRepo(db: AppDb): CitatorRepo = CitatorRepoImpl(db)
    @Provides @Singleton fun provideBookmarksRepo(db: AppDb): BookmarksRepo = BookmarksRepoImpl(db)
    @Provides @Singleton fun provideCollectionsRepo(db: AppDb): CollectionsRepo = CollectionsRepoImpl(db)
    @Provides @Singleton fun provideNotesRepo(db: AppDb): NotesRepo = NotesRepoImpl(db)
    @Provides @Singleton fun provideHistoryRepo(db: AppDb): HistoryRepo = HistoryRepoImpl(db)
    @Provides @Singleton fun provideSavedSearchesRepo(db: AppDb): SavedSearchesRepo = SavedSearchesRepoImpl(db)
    @Provides @Singleton fun provideTemplatesRepo(db: AppDb): TemplatesRepo = TemplatesRepoImpl(db)
    @Provides @Singleton fun provideChecklistsRepo(db: AppDb): ChecklistsRepo = ChecklistsRepoImpl(db)
    @Provides @Singleton fun provideDocketsRepo(db: AppDb): DocketsRepo = DocketsRepoImpl(db)
}
