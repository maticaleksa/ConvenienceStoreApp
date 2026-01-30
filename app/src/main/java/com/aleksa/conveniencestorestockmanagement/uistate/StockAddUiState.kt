package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class StockAddUiState(
    override val products: List<Product> = emptyList(),
    override val searchQuery: String = "",
    override val selectedProductId: String? = null,
    override val selectedProductName: String = "",
    override val currentStock: Int? = null,
    override val quantity: Int = 0,
    override val notes: String = "",
    override val isQuantityValid: Boolean = false,
) : StockUiState<StockAddUiState> {
    override fun withSearchQuery(query: String): StockAddUiState = copy(searchQuery = query)

    override fun withProducts(products: List<Product>): StockAddUiState = copy(products = products)

    override fun withSelection(
        selectedId: String?,
        selectedName: String,
    ): StockAddUiState = copy(selectedProductId = selectedId, selectedProductName = selectedName)

    override fun withQuantity(quantity: Int): StockAddUiState = copy(quantity = quantity)

    override fun withNotes(notes: String): StockAddUiState = copy(notes = notes)
}
