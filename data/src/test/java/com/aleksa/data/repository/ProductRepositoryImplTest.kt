package com.aleksa.data.repository

import com.aleksa.core.arch.event.DataCommand
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.data.database.CategoryEntity
import com.aleksa.data.database.ProductEntity
import com.aleksa.data.database.ProductWithCategorySupplier
import com.aleksa.data.database.SupplierEntity
import com.aleksa.data.remote.ProductDto
import com.aleksa.data.remote.SupplierDto
import com.aleksa.data.source.CategoryDataSource
import com.aleksa.data.source.ProductDataSource
import com.aleksa.data.source.ProductRemoteDataSource
import com.aleksa.data.source.SupplierDataSource
import com.aleksa.data.source.SupplierRemoteDataSource
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductRepositoryImplTest {
    @Test
    fun `observeSearch trims and lowercases query before passing to data source`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val productDataSource = RecordingProductDataSource()
        val repository = ProductRepositoryImpl(
            localDataSource = productDataSource,
            categoryDataSource = NoOpCategoryDataSource(),
            supplierDataSource = NoOpSupplierDataSource(),
            remoteDataSource = ErrorProductRemoteDataSource(),
            supplierRemoteDataSource = ErrorSupplierRemoteDataSource(),
            dataCommandBus = NoOpDataCommandBus(),
            syncCoordinator = SyncCoordinator(),
            coroutineScope = CoroutineScope(SupervisorJob() + dispatcher),
        )

        repository.observeSearch("  Milk  ").first()

        assertEquals("%milk%", productDataSource.lastSearchQuery)
    }

    private class RecordingProductDataSource : ProductDataSource {
        private val products = MutableStateFlow<List<ProductWithCategorySupplier>>(emptyList())
        var lastSearchQuery: String? = null

        override suspend fun getAll(): List<ProductWithCategorySupplier> = products.value

        override fun getAllFlow(): Flow<List<ProductWithCategorySupplier>> = products

        override fun searchFlow(query: String): Flow<List<ProductWithCategorySupplier>> {
            lastSearchQuery = query
            return products
        }

        override suspend fun count(): Int = products.value.size

        override suspend fun getAllIds(): List<String> = products.value.map { it.product.id }

        override suspend fun getSupplierIdsInUse(): List<String> =
            products.value.map { it.product.supplierId }.distinct()

        override suspend fun getById(id: String): ProductWithCategorySupplier? =
            products.value.firstOrNull { it.product.id == id }

        override fun getByIdFlow(id: String): Flow<ProductWithCategorySupplier?> =
            products.map { list -> list.firstOrNull { it.product.id == id } }

        override suspend fun upsertAll(products: List<ProductEntity>) = Unit

        override suspend fun clearAll() = Unit

        override suspend fun upsert(product: ProductEntity) = Unit

        override suspend fun upsertAndGet(product: ProductEntity): ProductWithCategorySupplier? = null

        override suspend fun delete(product: ProductEntity) = Unit

        override suspend fun deleteByIds(ids: Collection<String>) = Unit
    }

    private class NoOpCategoryDataSource : CategoryDataSource {
        override fun getAllFlow(): Flow<List<CategoryEntity>> = MutableStateFlow(emptyList())
        override suspend fun upsertAll(categories: List<CategoryEntity>) = Unit
    }

    private class NoOpSupplierDataSource : SupplierDataSource {
        override fun getAllFlow(): Flow<List<SupplierEntity>> = MutableStateFlow(emptyList())
        override fun searchFlow(query: String): Flow<List<SupplierEntity>> =
            MutableStateFlow(emptyList())
        override suspend fun getAll(): List<SupplierEntity> = emptyList()
        override suspend fun getAllIds(): List<String> = emptyList()
        override suspend fun upsertAll(suppliers: List<SupplierEntity>) = Unit
        override suspend fun upsert(supplier: SupplierEntity) = Unit
        override suspend fun deleteByIds(ids: Collection<String>) = Unit
    }

    private class ErrorProductRemoteDataSource : ProductRemoteDataSource {
        override suspend fun fetchAll(): NetworkResult<List<ProductDto>, ErrorResponse> =
            NetworkResult.Error(ErrorResponse(message = "no-op"))

        override suspend fun upsert(product: ProductDto): NetworkResult<ProductDto, ErrorResponse> =
            NetworkResult.Error(ErrorResponse(message = "no-op"))
    }

    private class ErrorSupplierRemoteDataSource : SupplierRemoteDataSource {
        override suspend fun fetchAll(): NetworkResult<List<SupplierDto>, ErrorResponse> =
            NetworkResult.Error(ErrorResponse(message = "no-op"))

        override suspend fun upsert(supplier: SupplierDto): NetworkResult<SupplierDto, ErrorResponse> =
            NetworkResult.Error(ErrorResponse(message = "no-op"))
    }

    private class NoOpDataCommandBus : DataCommandBus {
        override val events: Flow<DataCommand> = MutableSharedFlow()
        override suspend fun emit(event: DataCommand) = Unit
    }
}
