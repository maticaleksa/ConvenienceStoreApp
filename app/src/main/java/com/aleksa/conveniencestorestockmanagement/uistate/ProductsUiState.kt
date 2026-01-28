package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class ProductsUiState(
    val isLoading: Boolean = true,
    val items: List<Product> = emptyList()
)
