package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter

data class TransactionsUiState(
    val items: List<Transaction> = emptyList(),
    val isEmpty: Boolean = true,
    val selectedTypes: Set<TransactionType> = emptySet(),
    val dateFilter: TransactionDateFilter = TransactionDateFilter.ALL,
)
