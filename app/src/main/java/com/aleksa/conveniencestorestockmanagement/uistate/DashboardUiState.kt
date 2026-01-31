package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction

data class DashboardUiState(
    val lowStock: List<Product> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val lowStockExpanded: Boolean = false,
    val recentExpanded: Boolean = false,
    val isSyncing: Boolean = false,
)
