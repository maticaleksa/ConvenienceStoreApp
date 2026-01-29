package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Transaction

data class TransactionsUiState(
    val items: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true,
)
