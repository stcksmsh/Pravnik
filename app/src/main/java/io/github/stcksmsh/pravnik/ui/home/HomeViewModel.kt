package io.github.stcksmsh.pravnik.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.stcksmsh.pravnik.domain.model.Document
import io.github.stcksmsh.pravnik.domain.model.DocumentType
import io.github.stcksmsh.pravnik.domain.repo.DocumentsRepo
import io.github.stcksmsh.pravnik.domain.repo.HistoryRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documentsRepo: DocumentsRepo,
    private val historyRepo: HistoryRepo,
) : ViewModel() {
    private val _docs = MutableStateFlow<List<Document>>(emptyList())
    val docs: StateFlow<List<Document>> = _docs.asStateFlow()

    private val _typeFilter = MutableStateFlow<Set<DocumentType>>(emptySet())
    val typeFilter: StateFlow<Set<DocumentType>> = _typeFilter.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _docs.value = documentsRepo.listAll()
        }
    }

    fun toggleType(t: DocumentType) {
        val cur = _typeFilter.value.toMutableSet()
        if (!cur.add(t)) cur.remove(t)
        _typeFilter.value = cur
    }
}
