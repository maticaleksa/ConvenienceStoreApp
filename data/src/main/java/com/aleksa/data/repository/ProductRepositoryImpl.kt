package com.aleksa.data.repository

import com.aleksa.core.arch.coroutines.AppScope
import com.aleksa.data.database.toDomain
import com.aleksa.data.database.toEntity
import com.aleksa.data.database.ProductEntity
import com.aleksa.data.source.ProductDataSource
import com.aleksa.data.source.ProductRemoteDataSource
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.error.ProductSyncError
import com.aleksa.domain.event.ProductDataCommand
import com.aleksa.domain.event.ProductDataCommand.RefreshAll
import com.aleksa.domain.model.Product
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepositoryImpl @Inject constructor(
    private val localDataSource: ProductDataSource,
    private val remoteDataSource: ProductRemoteDataSource,
    private val dataCommandBus: DataCommandBus,
    syncCoordinator: SyncCoordinator,
    @AppScope private val coroutineScope: CoroutineScope,
) : ProductRepository {
    val syncChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)

    init {
        coroutineScope.launch {
            dataCommandBus.events
                .filterIsInstance<ProductDataCommand>()
                .collectLatest { event ->
                    when (event) {
                        RefreshAll -> refreshAllProducts()
                    }
                }
        }
        coroutineScope.launch {
            refreshAllProducts()
        }

    }

    override fun observeAll(): Flow<List<Product>> {
        return localDataSource.getAllFlow().map { productList ->
            productList.map { product ->
                product.toDomain()
            }
        }
    }

    override fun observeSearch(query: String): Flow<List<Product>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return observeAll()
        val likeQuery = "%${trimmed.lowercase()}%"
        return localDataSource.searchFlow(likeQuery).map { productList ->
            productList.map { product ->
                product.toDomain()
            }
        }
    }

    private suspend fun refreshAllProducts() {
        syncChannel.execute {
            val networkResult = remoteDataSource.fetchAll()

            when (networkResult) {
                is NetworkResult.Success -> {
                    val fresh = networkResult.data.map { it.toEntity() }
                    localDataSource.upsertAll(fresh)
                    removeProductsThatNoLongerExistOnRemote(fresh)
                }

                is NetworkResult.Error -> {
                    syncChannel.reportError(
                        error = ProductSyncError.Network(
                            message = networkResult.error.message,
                            code = networkResult.error.code,
                            details = networkResult.error.details
                        )
                    )
                    return@execute
                }

            }
        }
    }

    private suspend fun removeProductsThatNoLongerExistOnRemote(
        freshProducts: List<ProductEntity>,
    ) {
        val serverIds = freshProducts.map { it.id }.toSet()
        val localIds = localDataSource.getAllIds().toSet()
        val deletedIds = localIds - serverIds
        if (deletedIds.isNotEmpty()) {
            localDataSource.deleteByIds(deletedIds)
        }
    }
}
