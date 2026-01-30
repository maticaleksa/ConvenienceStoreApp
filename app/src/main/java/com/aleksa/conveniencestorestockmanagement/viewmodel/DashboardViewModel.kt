package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.DashboardUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.SyncState
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.domain.usecases.LowStockProductsUseCase
import com.aleksa.domain.usecases.RecentTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val lowStockProductsUseCase: LowStockProductsUseCase,
    private val recentTransactionsUseCase: RecentTransactionsUseCase,
    syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)

    init {
        observeLowStockProducts()
        observeRecentTransactions()
        observeSyncErrors()
    }

    private fun observeLowStockProducts() {
        lowStockProductsUseCase()
            .onEach { items ->
                _uiState.value = _uiState.value.copy(lowStock = items)
            }
            .launchIn(viewModelScope)
    }

    private fun observeRecentTransactions() {
        recentTransactionsUseCase(days = 5)
            .onEach { recent ->
                _uiState.value = _uiState.value.copy(recentTransactions = recent)
            }
            .launchIn(viewModelScope)
    }

    private fun observeSyncErrors() {
        syncChannel.state
            .onEach { state ->
                if (state is SyncState.Error) {
                    val message = state.error.message
                        ?: state.throwable?.message
                        ?: "Sync failed"
                    _events.tryEmit(UiEvent.Message(message))
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleLowStockExpanded() {
        _uiState.value = _uiState.value.copy(lowStockExpanded = !_uiState.value.lowStockExpanded)
    }

    fun toggleRecentExpanded() {
        _uiState.value = _uiState.value.copy(recentExpanded = !_uiState.value.recentExpanded)
    }
}
