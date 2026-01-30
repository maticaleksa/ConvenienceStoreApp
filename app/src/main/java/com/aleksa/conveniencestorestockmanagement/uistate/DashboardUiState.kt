package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class DashboardUiState(
    val lowStock: List<Product> = emptyList(),
    val errorMessage: String? = null,
)
