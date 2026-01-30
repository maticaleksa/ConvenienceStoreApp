package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class StockSaleUiState(
    override val products: List<Product> = emptyList(),
    override val searchQuery: String = "",
    override val selectedProductId: String? = null,
    override val selectedProductName: String = "",
    override val currentStock: Int? = null,
    override val quantity: Int = 0,
    override val notes: String = "",
    override val isQuantityValid: Boolean = false,
    override val errorMessage: String? = null,
) : StockUiState<StockSaleUiState> {
    override fun withSearchQuery(query: String): StockSaleUiState = copy(searchQuery = query)

    override fun withProducts(products: List<Product>): StockSaleUiState = copy(products = products)

    override fun withSelection(
        selectedId: String?,
        selectedName: String,
    ): StockSaleUiState = copy(selectedProductId = selectedId, selectedProductName = selectedName)

    override fun withQuantity(quantity: Int): StockSaleUiState = copy(quantity = quantity)

    override fun withNotes(notes: String): StockSaleUiState = copy(notes = notes)

    override fun withErrorMessage(message: String?): StockSaleUiState =
        copy(errorMessage = message)
}
