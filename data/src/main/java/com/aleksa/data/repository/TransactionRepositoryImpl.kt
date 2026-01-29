package com.aleksa.data.repository

import com.aleksa.data.database.TransactionEntity
import com.aleksa.data.database.toDomain
import com.aleksa.data.database.toEntity
import com.aleksa.data.remote.toDto
import com.aleksa.data.source.TransactionDataSource
import com.aleksa.data.source.TransactionRemoteDataSource
import com.aleksa.domain.TransactionRepository
import com.aleksa.domain.model.Transaction
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.aleksa.core.arch.coroutines.AppScope

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    @AppScope private val coroutineScope: CoroutineScope,
) : TransactionRepository {

    init {
        coroutineScope.launch {
            refreshAllTransactions()
        }
    }

    override fun observeAll(): Flow<List<Transaction>> {
        return localDataSource.getAllFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun upsert(transaction: Transaction) {
        val remoteResult = remoteDataSource.upsert(transaction.toDto())
        when (remoteResult) {
            is NetworkResult.Success -> {
                localDataSource.upsert(remoteResult.data.toEntity())
            }
            is NetworkResult.Error -> {
                localDataSource.upsert(transaction.toEntity())
            }
        }
    }

    private suspend fun refreshAllTransactions() {
        val networkResult = remoteDataSource.fetchAll()
        when (networkResult) {
            is NetworkResult.Success -> {
                val fresh = networkResult.data.map { it.toEntity() }
                localDataSource.upsertAll(fresh)
                removeTransactionsThatNoLongerExistOnRemote(fresh)
            }
            is NetworkResult.Error -> {
                // ignore for now
            }
        }
    }

    private suspend fun removeTransactionsThatNoLongerExistOnRemote(
        freshTransactions: List<TransactionEntity>,
    ) {
        val serverIds = freshTransactions.map { it.id }.toSet()
        val localIds = localDataSource.getAllIds().toSet()
        val deletedIds = localIds - serverIds
        if (deletedIds.isNotEmpty()) {
            localDataSource.deleteByIds(deletedIds)
        }
    }
}
