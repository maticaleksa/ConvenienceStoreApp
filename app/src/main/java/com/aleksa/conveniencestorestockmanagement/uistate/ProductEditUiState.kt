package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Supplier

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
    val supplierId: String? = null,
    val supplierName: String = "",
    val suppliers: List<Supplier> = emptyList(),
    val currentStockLevel: String = "",
    val minimumStockLevel: String = ""
) {
    enum class Mode { ADD, EDIT }
}
