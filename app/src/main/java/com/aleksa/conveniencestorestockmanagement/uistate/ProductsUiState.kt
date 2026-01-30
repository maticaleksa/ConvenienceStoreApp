package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Product

data class ProductsUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val items: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val searchQuery: String = "",
    val isEmpty: Boolean = true,
    val isSearchActive: Boolean = false,
    val errorMessage: String? = null,
)
