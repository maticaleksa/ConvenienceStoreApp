package com.aleksa.domain

import com.aleksa.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Access to transaction data sources and persistence operations.
 */
interface TransactionRepository {
    /**
     * Observes all transactions.
     */
    fun observeAll(): Flow<List<Transaction>>

    /**
     * Inserts or updates the provided transaction.
     */
    suspend fun upsert(transaction: Transaction)
}
