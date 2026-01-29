package com.aleksa.data.repository

import com.aleksa.data.database.SupplierEntity
import com.aleksa.data.database.toDomain
import com.aleksa.data.database.toEntity
import com.aleksa.data.remote.toDto
import com.aleksa.data.source.SupplierDataSource
import com.aleksa.data.source.SupplierRemoteDataSource
import com.aleksa.domain.SupplierRepository
import com.aleksa.domain.model.Supplier
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import com.aleksa.core.arch.coroutines.AppScope

@Singleton
class SupplierRepositoryImpl @Inject constructor(
    private val localDataSource: SupplierDataSource,
    private val remoteDataSource: SupplierRemoteDataSource,
    @AppScope private val coroutineScope: CoroutineScope,
) : SupplierRepository {

    init {
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
        val networkResult = remoteDataSource.fetchAll()
        when (networkResult) {
            is NetworkResult.Success -> {
                val fresh = networkResult.data.map { it.toEntity() }
                localDataSource.upsertAll(fresh)
                removeSuppliersThatNoLongerExistOnRemote(fresh)
            }
            is NetworkResult.Error -> {
                // ignore for now
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
