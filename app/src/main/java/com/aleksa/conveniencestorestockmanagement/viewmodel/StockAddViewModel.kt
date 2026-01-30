package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.StockAddUiState
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.StockTransactionResult
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.ApplyStockTransactionUseCase
import com.aleksa.domain.usecases.ProductSearchUseCase
import com.aleksa.data.repository.StockTransactionsSyncChannelKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockAddViewModel @Inject constructor(
    productSearchUseCase: ProductSearchUseCase,
    productRepository: ProductRepository,
    syncCoordinator: SyncCoordinator,
    private val applyStockTransactionUseCase: ApplyStockTransactionUseCase,
) : BaseStockViewModel<StockAddUiState>(
    productSearchUseCase = productSearchUseCase,
    productRepository = productRepository,
    syncCoordinator = syncCoordinator,
    syncChannelKey = StockTransactionsSyncChannelKey,
    emptyState = StockAddUiState()
) {

    override fun buildUiState(
        currentStock: Int?,
        quantity: Int,
        selectedId: String?,
        selectedName: String,
        notes: String,
    ): StockAddUiState {
        val isQuantityValid = selectedId != null && quantity > 0
        return StockAddUiState(
            products = products.value,
            searchQuery = searchQuery.value,
            selectedProductId = selectedId,
            selectedProductName = selectedName,
            currentStock = currentStock,
            quantity = quantity,
            notes = notes,
            isQuantityValid = isQuantityValid,
        )
    }

    override fun getMaxQuantity(currentStock: Int?): Int? = null

    override fun save() {
        ensureActive()
        viewModelScope.launch {
            val product = currentProduct() ?: return@launch
            val state = currentState()
            val qty = state.quantity.coerceAtLeast(0)
            if (qty == 0) return@launch
            when (
                val result = applyStockTransactionUseCase(
                    product = product,
                    quantity = qty,
                    notes = state.notes,
                    type = TransactionType.RESTOCK,
                )
            ) {
                is StockTransactionResult.Success -> {
                    _uiState.update { it.copy(quantity = 0, notes = "") }
                }
                is StockTransactionResult.Error -> {
                    emitMessage(result.message)
                }
            }
        }
    }
}
