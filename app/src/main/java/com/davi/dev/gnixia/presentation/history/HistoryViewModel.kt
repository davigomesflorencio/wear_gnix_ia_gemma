package com.davi.dev.gnixia.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.davi.dev.gnixia.GnixiaApplication
import com.davi.dev.gnixia.domain.model.HistoryEntry
import com.davi.dev.gnixia.domain.usecase.ClearHistoryUseCase
import com.davi.dev.gnixia.domain.usecase.ObserveHistoryEntryUseCase
import com.davi.dev.gnixia.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    observeHistory: ObserveHistoryUseCase,
    private val observeHistoryEntry: ObserveHistoryEntryUseCase,
    private val clearHistory: ClearHistoryUseCase
) : ViewModel() {

    val entries: StateFlow<List<HistoryEntry>> = observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun entry(id: Long): Flow<HistoryEntry?> = observeHistoryEntry(id)

    fun clear() {
        viewModelScope.launch { clearHistory() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as GnixiaApplication).container
                HistoryViewModel(
                    container.observeHistory,
                    container.observeHistoryEntry,
                    container.clearHistory
                )
            }
        }
    }
}
