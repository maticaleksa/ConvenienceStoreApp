package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

interface StockUiState<T : StockUiState<T>> {
    val products: List<Product>
    val searchQuery: String
    val selectedProductId: String?
    val selectedProductName: String
    val currentStock: Int?
    val quantity: Int
    val notes: String
    val isQuantityValid: Boolean
    val errorMessage: String?

    fun withSearchQuery(query: String): T
    fun withProducts(products: List<Product>): T
    fun withSelection(selectedId: String?, selectedName: String): T
    fun withQuantity(quantity: Int): T
    fun withNotes(notes: String): T
    fun withErrorMessage(message: String?): T
}
