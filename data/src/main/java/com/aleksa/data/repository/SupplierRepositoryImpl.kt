package com.aleksa.data.repository

import com.aleksa.data.database.SupplierEntity
import com.aleksa.data.database.toDomain
import com.aleksa.data.database.toEntity
import com.aleksa.data.remote.toDto
import com.aleksa.data.source.SupplierDataSource
import com.aleksa.data.source.SupplierRemoteDataSource
import com.aleksa.domain.event.SupplierDataCommand
import com.aleksa.domain.event.SupplierDataCommand.RefreshAll
import com.aleksa.domain.SupplierRepository
import com.aleksa.domain.model.Supplier
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.aleksa.core.arch.coroutines.AppScope
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.UnknownSyncError

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val localDataSource: SupplierDataSource,
    private val remoteDataSource: SupplierRemoteDataSource,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
    @AppScope private val coroutineScope: CoroutineScope,
) : SupplierRepository {
    val syncChannel = syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)

    init {
        coroutineScope.launch {
            dataCommandBus.events
                .filterIsInstance<SupplierDataCommand>()
                .collectLatest { event ->
                    when (event) {
                        RefreshAll -> refreshAllSuppliers()
                    }
                }
        }
        coroutineScope.launch {
            refreshAllSuppliers()
        }
    }

    override fun observeAll(): Flow<List<Supplier>> {
        return localDataSource.getAllFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeSearch(query: String): Flow<List<Supplier>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return observeAll()
        val likeQuery = "%${trimmed.lowercase()}%"
        return localDataSource.searchFlow(likeQuery).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun upsert(supplier: Supplier) {
        val remoteResult = remoteDataSource.upsert(supplier.toDto())
        when (remoteResult) {
            is NetworkResult.Success -> {
                localDataSource.upsert(remoteResult.data.toEntity())
            }
            is NetworkResult.Error -> {
                localDataSource.upsert(supplier.toEntity())
            }
        }
    }

    private suspend fun refreshAllSuppliers() {
        syncChannel.execute {
            val networkResult = remoteDataSource.fetchAll()
            when (networkResult) {
                is NetworkResult.Success -> {
                    val fresh = networkResult.data.map { it.toEntity() }
                    localDataSource.upsertAll(fresh)
                    removeSuppliersThatNoLongerExistOnRemote(fresh)
                }
                is NetworkResult.Error -> {
                    syncChannel.reportError(
                        UnknownSyncError(message = networkResult.error.message)
                    )
                }
            }
        }
    }

    private suspend fun removeSuppliersThatNoLongerExistOnRemote(
        freshSuppliers: List<SupplierEntity>,
    ) {
        val serverIds = freshSuppliers.map { it.id }.toSet()
        val localIds = localDataSource.getAllIds().toSet()
        val deletedIds = localIds - serverIds
        if (deletedIds.isNotEmpty()) {
            localDataSource.deleteByIds(deletedIds)
        }
    }
}
