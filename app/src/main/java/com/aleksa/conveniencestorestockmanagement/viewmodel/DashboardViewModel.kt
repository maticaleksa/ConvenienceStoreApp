package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.DashboardUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncChannel
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.SyncState
import com.aleksa.data.repository.TransactionsSyncChannelKey
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.domain.event.ProductDataCommand.RefreshAll as RefreshProducts
import com.aleksa.domain.event.TransactionDataCommand.RefreshAll as RefreshTransactions
import com.aleksa.domain.usecases.LowStockProductsUseCase
import com.aleksa.domain.usecases.RecentTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val lowStockProductsUseCase: LowStockProductsUseCase,
    private val recentTransactionsUseCase: RecentTransactionsUseCase,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
    private val productsSyncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
    private val transactionsSyncChannel = syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)

    init {
        observeLowStockProducts()
        observeRecentTransactions()
        observeSyncErrors()
        observeSyncStatus()
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
        observeSyncChannelErrors(productsSyncChannel)
        observeSyncChannelErrors(transactionsSyncChannel)
    }

    private fun observeSyncChannelErrors(channel: SyncChannel) {
        channel.state
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

    private fun observeSyncStatus() {
        combine(
            productsSyncChannel.isActive,
            transactionsSyncChannel.isActive,
        ) { productsActive, transactionsActive ->
            productsActive || transactionsActive
        }.onEach { isSyncing ->
            _uiState.value = _uiState.value.copy(isSyncing = isSyncing)
        }.launchIn(viewModelScope)
    }

    fun toggleLowStockExpanded() {
        _uiState.value = _uiState.value.copy(lowStockExpanded = !_uiState.value.lowStockExpanded)
    }

    fun toggleRecentExpanded() {
        _uiState.value = _uiState.value.copy(recentExpanded = !_uiState.value.recentExpanded)
    }

    fun refresh() {
        viewModelScope.launch {
            dataCommandBus.emit(RefreshProducts)
            dataCommandBus.emit(RefreshTransactions)
        }
    }
}
