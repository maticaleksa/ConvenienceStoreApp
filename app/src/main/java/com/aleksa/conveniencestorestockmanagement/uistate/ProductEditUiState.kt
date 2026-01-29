package com.aleksa.conveniencestorestockmanagement.uistate

data class ProductEditUiState(
    val mode: Mode = Mode.ADD,
    val productId: String? = null,
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val barcode: String = "",
    val currentStockLevel: String = "",
    val minimumStockLevel: String = ""
) {
    enum class Mode { ADD, EDIT }
}
