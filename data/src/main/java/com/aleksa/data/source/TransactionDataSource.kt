package com.aleksa.data.source

import com.aleksa.data.database.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionDataSource {
    fun getAllFlow(): Flow<List<TransactionEntity>>
    suspend fun getAll(): List<TransactionEntity>
    suspend fun getAllIds(): List<String>
    suspend fun upsertAll(transactions: List<TransactionEntity>)
    suspend fun upsert(transaction: TransactionEntity)
    suspend fun deleteByIds(ids: Collection<String>)
}
