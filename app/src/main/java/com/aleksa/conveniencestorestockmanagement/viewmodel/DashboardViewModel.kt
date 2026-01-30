package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.DashboardUiState
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.SyncState
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.domain.usecases.LowStockProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val lowStockProductsUseCase: LowStockProductsUseCase,
    syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)

    init {
        lowStockProductsUseCase()
            .onEach { items ->
                _uiState.value = _uiState.value.copy(lowStock = items)
            }
            .launchIn(viewModelScope)
        observeSyncErrors()
    }

    private fun observeSyncErrors() {
        syncChannel.state
            .onEach { state ->
                if (state is SyncState.Error) {
                    val message = state.error.message
                        ?: state.throwable?.message
                        ?: "Sync failed"
                    _uiState.value = _uiState.value.copy(errorMessage = message)
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
