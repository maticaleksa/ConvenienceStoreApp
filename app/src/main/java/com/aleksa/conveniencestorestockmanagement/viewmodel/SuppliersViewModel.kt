package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.SuppliersUiState
import com.aleksa.domain.SupplierRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.data.repository.SuppliersSyncChannelKey
import com.aleksa.domain.event.SupplierDataCommand.RefreshAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SuppliersViewModel @Inject constructor(
    private val supplierRepository: SupplierRepository,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SuppliersUiState())
    val uiState: StateFlow<SuppliersUiState> = _uiState.asStateFlow()
    private val searchQuery = MutableStateFlow("")
    private val syncChannel = syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)

    init {
        observeSuppliers()
        observeSearchQuery()
        observeSyncStatus()
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun observeSuppliers() {
        searchQuery.debounce(300L).distinctUntilChanged()
            .flatMapLatest { query -> supplierRepository.observeSearch(query) }
            .onEach { items ->
                _uiState.update { it.copy(items = items, isEmpty = items.isEmpty()) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearchQuery() {
        searchQuery.onEach { query ->
            _uiState.update {
                it.copy(searchQuery = query, isSearchActive = query.isNotBlank())
            }
        }.launchIn(viewModelScope)
    }

    private fun observeSyncStatus() {
        syncChannel.isActive
            .onEach { isSyncing ->
                _uiState.update { it.copy(isSyncing = isSyncing) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    fun refresh() {
        viewModelScope.launch {
            dataCommandBus.emit(RefreshAll)
        }
    }
}
