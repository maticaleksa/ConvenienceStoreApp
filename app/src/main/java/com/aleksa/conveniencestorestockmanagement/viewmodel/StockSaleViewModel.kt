package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.uistate.StockSaleUiState
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.ApplyStockTransactionUseCase
import com.aleksa.domain.usecases.ProductSearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class StockSaleViewModel @Inject constructor(
    productSearchUseCase: ProductSearchUseCase,
    productRepository: ProductRepository,
    private val applyStockTransactionUseCase: ApplyStockTransactionUseCase,
) : BaseStockViewModel<StockSaleUiState>(
    productSearchUseCase = productSearchUseCase,
    productRepository = productRepository,
    emptyState = StockSaleUiState()
) {

    override fun buildUiState(
        currentStock: Int?,
        quantity: Int,
        selectedId: String?,
        selectedName: String,
        notes: String,
    ): StockSaleUiState {
        val maxStock = currentStock ?: 0
        val isQuantityValid =
            selectedId != null && quantity > 0 && maxStock > 0 && quantity <= maxStock
        return StockSaleUiState(
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

    override fun getMaxQuantity(currentStock: Int?): Int? = currentStock

    override fun save() {
        ensureActive()
        viewModelScope.launch {
            val product = currentProduct() ?: return@launch
            val state = currentState()
            val max = product.currentStockLevel
            val qty = state.quantity.coerceIn(0, max)
            if (qty == 0) return@launch
            applyStockTransactionUseCase(
                product = product,
                quantity = qty,
                notes = state.notes,
                type = TransactionType.SALE,
            )
            _uiState.update { it.copy(quantity = 0, notes = "") }
        }
    }
}
