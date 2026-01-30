package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.ProductsUiState
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.SyncState
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.usecases.ProductFilters
import com.aleksa.domain.usecases.ProductSearchUseCase
import com.aleksa.domain.event.ProductDataCommand.RefreshAll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productSearchUseCase: ProductSearchUseCase,
    private val categoryRepository: CategoryRepository,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")
    private val selectedCategoryIds = MutableStateFlow<Set<String>>(emptySet())
    private val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
    private val _addProductEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val addProductEvents = _addProductEvents.asSharedFlow()

    init {
        observeProducts()
        observeCategories()
        observeSyncStatus()
        observeSyncErrors()
        observeSearchQuery()
        observeSelectedCategories()
    }

    private fun observeProducts() {
        combine(
            searchQuery.debounce(300L).distinctUntilChanged(),
            selectedCategoryIds
        ) { query, ids -> query to ids }
            .flatMapLatest { (query, ids) ->
                productSearchUseCase(query, ProductFilters(categoryIds = ids))
            }
            .onEach { products ->
                _uiState.update {
                    it.copy(
                        items = products,
                        isEmpty = products.isEmpty(),
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeCategories() {
        categoryRepository.observeAll()
            .onEach { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSyncStatus() {
        syncChannel.isActive
            .onEach { isSyncing ->
                _uiState.update { it.copy(isSyncing = isSyncing) }
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
                    _uiState.update { it.copy(errorMessage = message) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSearchQuery() {
        searchQuery
            .onEach { query ->
                _uiState.update {
                    it.copy(
                        searchQuery = query,
                        isSearchActive = query.isNotBlank()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSelectedCategories() {
        selectedCategoryIds
            .onEach { ids ->
                _uiState.update { it.copy(selectedCategoryIds = ids) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery.value = query
    }

    fun clearSearch() {
        searchQuery.value = ""
    }

    fun updateSelectedCategories(ids: Set<String>) {
        selectedCategoryIds.value = ids
    }

    fun refresh() {
        viewModelScope.launch {
            dataCommandBus.emit(RefreshAll)
        }
    }

    fun onAddProductClicked() {
        _addProductEvents.tryEmit(Unit)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
