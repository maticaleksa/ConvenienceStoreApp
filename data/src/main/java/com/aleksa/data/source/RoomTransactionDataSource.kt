package com.aleksa.data.source

import com.aleksa.data.database.TransactionDao
import com.aleksa.data.database.TransactionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RoomTransactionDataSource @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionDataSource {
    override fun getAllFlow(): Flow<List<TransactionEntity>> = transactionDao.getAllFlow()

    override suspend fun getAll(): List<TransactionEntity> = transactionDao.getAll()

    override suspend fun getAllIds(): List<String> = transactionDao.getAllIds()

    override suspend fun upsertAll(transactions: List<TransactionEntity>) =
        transactionDao.upsertAll(transactions)

    override suspend fun upsert(transaction: TransactionEntity) = transactionDao.upsert(transaction)

    override suspend fun deleteByIds(ids: Collection<String>) = transactionDao.deleteByIds(ids)
}
