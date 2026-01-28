package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class ProductsUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val items: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isEmpty: Boolean = false,
    val isSearchActive: Boolean = false
)