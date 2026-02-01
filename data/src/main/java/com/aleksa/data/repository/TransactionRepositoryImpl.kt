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
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.domain.error.TransactionSyncError
import com.aleksa.domain.event.TransactionDataCommand
import com.aleksa.domain.event.TransactionDataCommand.RefreshAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.aleksa.core.arch.coroutines.AppScope

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
    @AppScope private val coroutineScope: CoroutineScope,
) : TransactionRepository {
    private val syncChannel = syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)

    init {
        coroutineScope.launch {
            dataCommandBus.events
                .filterIsInstance<TransactionDataCommand>()
                .collectLatest { event ->
                    when (event) {
                        RefreshAll -> syncChannel.execute { refreshAllTransactions() }
                    }
                }
        }
        coroutineScope.launch {
            syncChannel.execute {
                refreshAllTransactions()
            }
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
                syncChannel.reportError(
                    TransactionSyncError.Network(
                        message = networkResult.error.message ?: "Failed to refresh transactions",
                        code = networkResult.error.code,
                        details = networkResult.error.details
                    ),
                )
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
