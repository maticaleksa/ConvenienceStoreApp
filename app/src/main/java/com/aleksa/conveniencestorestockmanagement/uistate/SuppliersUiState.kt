package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Supplier

data class SuppliersUiState(
    val items: List<Supplier> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isEmpty: Boolean = true,
)
