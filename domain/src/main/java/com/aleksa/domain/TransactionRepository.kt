package com.aleksa.domain

import com.aleksa.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    suspend fun upsert(transaction: Transaction)
}
