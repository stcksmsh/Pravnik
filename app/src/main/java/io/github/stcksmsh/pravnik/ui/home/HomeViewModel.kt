package io.github.stcksmsh.pravnik.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.stcksmsh.pravnik.domain.model.Document
import io.github.stcksmsh.pravnik.domain.model.DocumentType
import io.github.stcksmsh.pravnik.domain.model.Bookmark
import io.github.stcksmsh.pravnik.domain.model.History
import io.github.stcksmsh.pravnik.domain.model.SavedSearch
import io.github.stcksmsh.pravnik.domain.model.SearchQuery
import io.github.stcksmsh.pravnik.domain.repo.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documentsRepo: DocumentsRepo,
    private val historyRepo: HistoryRepo,
    private val bookmarksRepo: BookmarksRepo,
    private val savedSearchesRepo: SavedSearchesRepo,
    private val searchQueriesRepo: SearchQueriesRepo,
) : ViewModel() {
    private val _docs = MutableStateFlow<List<Document>>(emptyList())
    val docs: StateFlow<List<Document>> = _docs.asStateFlow()

    private val _typeFilter = MutableStateFlow<Set<DocumentType>>(emptySet())
    val typeFilter: StateFlow<Set<DocumentType>> = _typeFilter.asStateFlow()

    private val _recentQueries = MutableStateFlow<List<SearchQuery>>(emptyList())
    val recentQueries: StateFlow<List<SearchQuery>> = _recentQueries.asStateFlow()

    private val _recentBookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val recentBookmarks: StateFlow<List<Bookmark>> = _recentBookmarks.asStateFlow()

    private val _recentHistory = MutableStateFlow<List<History>>(emptyList())
    val recentHistory: StateFlow<List<History>> = _recentHistory.asStateFlow()

    private val _starredDocs = MutableStateFlow<List<Document>>(emptyList())
    val starredDocs: StateFlow<List<Document>> = _starredDocs.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _docs.value = documentsRepo.listAll()
            _starredDocs.value = documentsRepo.listStarred()
            _recentBookmarks.value = bookmarksRepo.listBookmarks().take(5)
            _recentHistory.value = historyRepo.recent(5)
            _recentQueries.value = searchQueriesRepo.recent(5)
        }
    }

    fun toggleType(t: DocumentType) {
        val cur = _typeFilter.value.toMutableSet()
        if (!cur.add(t)) cur.remove(t)
        _typeFilter.value = cur
    }
}
