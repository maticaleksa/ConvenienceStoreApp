package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Category

data class ProductEditUiState(
    val mode: Mode = Mode.ADD,
    val productId: String? = null,
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val barcode: String = "",
    val categoryId: String? = null,
    val categoryName: String = "",
    val categories: List<Category> = emptyList(),
    val currentStockLevel: String = "",
    val minimumStockLevel: String = ""
) {
    enum class Mode { ADD, EDIT }
}
