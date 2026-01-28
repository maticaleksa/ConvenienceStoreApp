package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.event.ProductDataCommand.RefreshAll
import com.aleksa.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class ProductsViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")
    private val _uiState = MutableStateFlow(ProductsUiState(isLoading = true))
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    private val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)

    init {
        combine(
            searchQuery
                .debounce(300L)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        productRepository.observeAll()
                    } else {
                        productRepository.observeSearch(query)
                    }
                },
            syncChannel.isActive,
            searchQuery
        ) { products, isSyncing, query ->
            ProductsUiState(
                isLoading = false,
                isSyncing = isSyncing,
                items = products,
                searchQuery = query,
                isEmpty = products.isEmpty(),
                isSearchActive = query.isNotBlank()
            )
        }.onEach { _uiState.value = it }
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

data class ProductsUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val items: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isEmpty: Boolean = false,
    val isSearchActive: Boolean = false
)
