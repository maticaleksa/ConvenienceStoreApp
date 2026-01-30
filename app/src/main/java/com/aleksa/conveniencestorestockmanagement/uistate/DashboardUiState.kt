package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product

data class DashboardUiState(
    val lowStock: List<Product> = emptyList(),
    val recentTransactions: List<com.aleksa.domain.model.Transaction> = emptyList(),
    val lowStockExpanded: Boolean = false,
    val recentExpanded: Boolean = false,
    val isSyncing: Boolean = false,
)
