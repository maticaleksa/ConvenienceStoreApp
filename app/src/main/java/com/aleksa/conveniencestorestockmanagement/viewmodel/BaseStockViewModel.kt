package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.StockUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.sync.SyncChannel
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.SyncState
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.Product
import com.aleksa.domain.usecases.ProductSearchUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.aleksa.core.arch.sync.SyncChannelKey
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
abstract class BaseStockViewModel<T : StockUiState<T>>(
    private val productSearchUseCase: ProductSearchUseCase,
    private val productRepository: ProductRepository,
    private val syncCoordinator: SyncCoordinator,
    private val syncChannelKey: SyncChannelKey,
    emptyState: T,
) : ViewModel() {

    protected val searchQuery = MutableStateFlow("")
    protected val products = MutableStateFlow(emptyList<Product>())
    protected var allProducts: List<Product> = emptyList()

    protected val _uiState = MutableStateFlow(emptyState)
    val uiState: StateFlow<T> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var isActive: Boolean = false
    private var isSyncObserved: Boolean = false
    private var syncChannel: SyncChannel? = null

    protected fun ensureActive() {
        if (isActive) return
        isActive = true
        observeProducts()
        observeAllProducts()
        observeSyncState()
        updateUiState()
    }

    private fun observeProducts() {
        searchQuery
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { query -> productSearchUseCase(query) }
            .onEach { items ->
                products.value = items
                updateUiState()
            }
            .launchIn(viewModelScope)
    }

    private fun observeAllProducts() {
        productRepository.observeAll()
            .onEach { items ->
                allProducts = items
                updateUiState()
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        ensureActive()
        searchQuery.value = query
        _uiState.update { it.withSearchQuery(query) }
    }

    fun onProductInputChanged(productName: String) {
        ensureActive()
        val product = allProducts.firstOrNull { it.name == productName }
        _uiState.update { it.withSelection(product?.id, productName) }
        updateUiState()
    }

    fun onProductSelected(productName: String) {
        ensureActive()
        val product = allProducts.firstOrNull { it.name == productName }
        _uiState.update { it.withSelection(product?.id, productName) }
        updateUiState()
    }

    fun onQuantityChanged(value: String) {
        ensureActive()
        val parsed = value.toIntOrNull() ?: 0
        _uiState.update { state ->
            val clamped = clampQuantityForState(state, parsed)
            state.withQuantity(clamped)
        }
        updateUiState()
    }

    fun onNotesChanged(value: String) {
        ensureActive()
        _uiState.update { it.withNotes(value) }
    }

    protected fun emitMessage(message: String) {
        _events.tryEmit(UiEvent.Message(message))
    }

    fun incrementQuantity() {
        ensureActive()
        _uiState.update { state ->
            val newQty = state.quantity + 1
            val clamped = clampQuantityForState(state, newQty)
            state.withQuantity(clamped)
        }
        updateUiState()
    }

    fun decrementQuantity() {
        ensureActive()
        _uiState.update { state ->
            val newQty = (state.quantity - 1).coerceAtLeast(0)
            state.withQuantity(newQty)
        }
        updateUiState()
    }

    protected fun updateUiState() {
        val currentState = _uiState.value
        val product = currentState.selectedProductId?.let { id ->
            allProducts.firstOrNull { it.id == id }
        }

        if (product != null && product.name != currentState.selectedProductName) {
            _uiState.update { it.withSelection(currentState.selectedProductId, product.name) }
        }

        val currentStock = product?.currentStockLevel
        val max = getMaxQuantity(currentStock)
        val clampedQuantity = if (max != null) {
            currentState.quantity.coerceIn(0, max)
        } else {
            currentState.quantity.coerceAtLeast(0)
        }

        _uiState.value = buildUiState(
            currentStock = currentStock,
            quantity = clampedQuantity,
            selectedId = currentState.selectedProductId,
            selectedName = _uiState.value.selectedProductName,
            notes = currentState.notes
        )
    }

    protected abstract fun buildUiState(
        currentStock: Int?,
        quantity: Int,
        selectedId: String?,
        selectedName: String,
        notes: String,
    ): T

    protected abstract fun getMaxQuantity(currentStock: Int?): Int?

    private fun clampQuantityForState(state: T, quantity: Int): Int {
        val max = getMaxQuantity(state.currentStock)
        return if (max != null) {
            quantity.coerceIn(0, max)
        } else {
            quantity.coerceAtLeast(0)
        }
    }

    protected fun currentState(): T = _uiState.value

    protected fun currentProduct(): Product? {
        return currentState().selectedProductId?.let { id ->
            allProducts.firstOrNull { it.id == id }
        }
    }

    abstract fun save()

    private fun observeSyncState() {
        if (isSyncObserved) return
        isSyncObserved = true
        syncChannel = syncCoordinator.getOrCreateChannel(syncChannelKey)
        viewModelScope.launch {
            syncChannel?.state?.collect { state ->
                if (state is SyncState.Error) {
                    val message = state.error.message
                        ?: state.throwable?.message
                        ?: "Sync failed"
                    emitMessage(message)
                }
            }
        }
    }
}
