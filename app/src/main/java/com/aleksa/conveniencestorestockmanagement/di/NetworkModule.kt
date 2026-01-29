package com.aleksa.conveniencestorestockmanagement.di

import com.aleksa.conveniencestorestockmanagement.data.NetworkGreetingRepository
import com.aleksa.conveniencestorestockmanagement.domain.GreetingRepository
import com.aleksa.data.repository.ProductRepositoryImpl
import com.aleksa.data.repository.CategoryRepositoryImpl
import com.aleksa.data.repository.SupplierRepositoryImpl
import com.aleksa.data.repository.TransactionRepositoryImpl
import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.SupplierRepository
import com.aleksa.domain.TransactionRepository
import com.aleksa.data.fake.fakeProductsDtoList
import com.aleksa.data.fake.fakeSuppliersDtoList
import com.aleksa.data.fake.fakeTransactionsDtoList
import com.aleksa.data.remote.ProductDto
import com.aleksa.data.remote.SupplierDto
import com.aleksa.data.remote.TransactionDto
import com.aleksa.data.source.NetworkProductRemoteDataSource
import com.aleksa.data.source.NetworkSupplierRemoteDataSource
import com.aleksa.data.source.NetworkTransactionRemoteDataSource
import com.aleksa.data.source.ProductRemoteDataSource
import com.aleksa.data.source.SupplierRemoteDataSource
import com.aleksa.data.source.TransactionRemoteDataSource
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.api.ApiPaths
import com.aleksa.network.config.HttpClientFactory
import com.aleksa.network.config.NetworkConfig
import com.aleksa.network.fake.FakeNetworkExecutor
import com.aleksa.network.fake.FakeRequest
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import io.ktor.client.HttpClient
import com.aleksa.network.KtorNetworkExecutor
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import java.io.File
import android.util.Log

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val TAG = "FakeNetwork"

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = "https://fake.local",
            isDebug = true,
            timeoutMillis = 30_000L,
        )
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        config: NetworkConfig,
    ): HttpClient = HttpClientFactory.create(config)

    @Provides
    @Singleton
    fun provideKtorNetworkExecutor(
        client: HttpClient,
    ): KtorNetworkExecutor = KtorNetworkExecutor(client)

    @Provides
    @Singleton
    fun provideProductRemoteDataSource(
        dataSource: NetworkProductRemoteDataSource,
    ): ProductRemoteDataSource = dataSource

    @Provides
    @Singleton
    fun provideSupplierRemoteDataSource(
        dataSource: NetworkSupplierRemoteDataSource,
    ): SupplierRemoteDataSource = dataSource

    @Provides
    @Singleton
    fun provideTransactionRemoteDataSource(
        dataSource: NetworkTransactionRemoteDataSource,
    ): TransactionRemoteDataSource = dataSource

    @Provides
    @Singleton
    fun provideFakeNetworkExecutor(
        @ApplicationContext appContext: Context,
    ): FakeNetworkExecutor {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val storageFile = File(appContext.filesDir, "fake_products.json")
        var productsJson = loadOrSeedProductsJson(storageFile, json)
        val suppliersFile = File(appContext.filesDir, "fake_suppliers.json")
        var suppliersJson = loadOrSeedSuppliersJson(suppliersFile, json)
        val transactionsFile = File(appContext.filesDir, "fake_transactions.json")
        var transactionsJson = loadOrSeedTransactionsJson(transactionsFile, json)
        val handlers: Map<String, (FakeRequest) -> NetworkResult<Any, ErrorResponse>> =
            mapOf(
                "GET ${ApiPaths.PRODUCTS}" to { _: FakeRequest ->
                if (storageFile.exists()) {
                    productsJson = storageFile.readText()
                } else {
                    productsJson = loadOrSeedProductsJson(storageFile, json)
                }
                Log.d(TAG, "GET ${ApiPaths.PRODUCTS} bytes=${productsJson.length}")
                NetworkResult.Success(productsJson)
                },
                "POST ${ApiPaths.PRODUCTS}" to { request: FakeRequest ->
                val body = request.bodyText
                if (body == null) {
                    Log.d(TAG, "POST ${ApiPaths.PRODUCTS} missing body")
                    NetworkResult.Error(
                        ErrorResponse(message = "Missing body"),
                    )
                } else {
                    Log.d(TAG, "POST ${ApiPaths.PRODUCTS} bytes=${body.length}")
                    val dto = json.decodeFromString(ProductDto.serializer(), body)
                    val list = json.decodeFromString(
                        ListSerializer(ProductDto.serializer()),
                        productsJson,
                    )
                    val updated = list.filterNot { it.id == dto.id } + dto
                    productsJson = json.encodeToString(
                        ListSerializer(ProductDto.serializer()),
                        updated,
                    )
                    storageFile.writeText(productsJson)
                    Log.d(TAG, "POST ${ApiPaths.PRODUCTS} persisted bytes=${productsJson.length}")
                    NetworkResult.Success(json.encodeToString(ProductDto.serializer(), dto))
                }
                },
                "GET ${ApiPaths.SUPPLIERS}" to { _: FakeRequest ->
                if (suppliersFile.exists()) {
                    suppliersJson = suppliersFile.readText()
                } else {
                    suppliersJson = loadOrSeedSuppliersJson(suppliersFile, json)
                }
                Log.d(TAG, "GET ${ApiPaths.SUPPLIERS} bytes=${suppliersJson.length}")
                NetworkResult.Success(suppliersJson)
                },
                "POST ${ApiPaths.SUPPLIERS}" to { request: FakeRequest ->
                val body = request.bodyText
                if (body == null) {
                    Log.d(TAG, "POST ${ApiPaths.SUPPLIERS} missing body")
                    NetworkResult.Error(
                        ErrorResponse(message = "Missing body"),
                    )
                } else {
                    Log.d(TAG, "POST ${ApiPaths.SUPPLIERS} bytes=${body.length}")
                    val dto = json.decodeFromString(SupplierDto.serializer(), body)
                    val list = json.decodeFromString(
                        ListSerializer(SupplierDto.serializer()),
                        suppliersJson,
                    )
                    val updated = list.filterNot { it.id == dto.id } + dto
                    suppliersJson = json.encodeToString(
                        ListSerializer(SupplierDto.serializer()),
                        updated,
                    )
                    suppliersFile.writeText(suppliersJson)
                    Log.d(TAG, "POST ${ApiPaths.SUPPLIERS} persisted bytes=${suppliersJson.length}")
                    NetworkResult.Success(json.encodeToString(SupplierDto.serializer(), dto))
                }
                },
                "GET ${ApiPaths.TRANSACTIONS}" to { _: FakeRequest ->
                if (transactionsFile.exists()) {
                    transactionsJson = transactionsFile.readText()
                } else {
                    transactionsJson = loadOrSeedTransactionsJson(transactionsFile, json)
                }
                Log.d(TAG, "GET ${ApiPaths.TRANSACTIONS} bytes=${transactionsJson.length}")
                NetworkResult.Success(transactionsJson)
                },
                "POST ${ApiPaths.TRANSACTIONS}" to { request: FakeRequest ->
                val body = request.bodyText
                if (body == null) {
                    Log.d(TAG, "POST ${ApiPaths.TRANSACTIONS} missing body")
                    NetworkResult.Error(
                        ErrorResponse(message = "Missing body"),
                    )
                } else {
                    Log.d(TAG, "POST ${ApiPaths.TRANSACTIONS} bytes=${body.length}")
                    val dto = json.decodeFromString(TransactionDto.serializer(), body)
                    val list = json.decodeFromString(
                        ListSerializer(TransactionDto.serializer()),
                        transactionsJson,
                    )
                    val updated = list.filterNot { it.id == dto.id } + dto
                    transactionsJson = json.encodeToString(
                        ListSerializer(TransactionDto.serializer()),
                        updated,
                    )
                    transactionsFile.writeText(transactionsJson)
                    Log.d(TAG, "POST ${ApiPaths.TRANSACTIONS} persisted bytes=${transactionsJson.length}")
                    NetworkResult.Success(json.encodeToString(TransactionDto.serializer(), dto))
                }
                },
            )
        return FakeNetworkExecutor(
            routes = mapOf(
                "/greeting" to NetworkResult.Success("Hello from Fake Network"),
            ),
            handlers = handlers,
        )
    }

    private fun loadOrSeedProductsJson(
        file: File,
        json: Json,
    ): String {
        if (file.exists()) {
            return file.readText()
        }
        val seeded = json.encodeToString(
            ListSerializer(ProductDto.serializer()),
            fakeProductsDtoList,
        )
        file.writeText(seeded)
        return seeded
    }

    private fun loadOrSeedSuppliersJson(
        file: File,
        json: Json,
    ): String {
        if (file.exists()) {
            return file.readText()
        }
        val seeded = json.encodeToString(
            ListSerializer(SupplierDto.serializer()),
            fakeSuppliersDtoList,
        )
        file.writeText(seeded)
        return seeded
    }

    private fun loadOrSeedTransactionsJson(
        file: File,
        json: Json,
    ): String {
        if (file.exists()) {
            return file.readText()
        }
        val seeded = json.encodeToString(
            ListSerializer(TransactionDto.serializer()),
            fakeTransactionsDtoList,
        )
        file.writeText(seeded)
        return seeded
    }

    @Provides
    @Singleton
    fun provideNetworkExecutor(
        fakeNetworkExecutor: FakeNetworkExecutor,
    ): NetworkExecutor = fakeNetworkExecutor
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGreetingRepository(
        repository: NetworkGreetingRepository,
    ): GreetingRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        repository: ProductRepositoryImpl,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        repository: CategoryRepositoryImpl,
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindSupplierRepository(
        repository: SupplierRepositoryImpl,
    ): SupplierRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        repository: TransactionRepositoryImpl,
    ): TransactionRepository
}
