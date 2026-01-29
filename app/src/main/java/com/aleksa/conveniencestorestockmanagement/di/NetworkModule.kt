package com.aleksa.conveniencestorestockmanagement.di

import com.aleksa.conveniencestorestockmanagement.data.NetworkGreetingRepository
import com.aleksa.conveniencestorestockmanagement.domain.GreetingRepository
import com.aleksa.data.repository.ProductRepositoryImpl
import com.aleksa.data.repository.CategoryRepositoryImpl
import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.ProductRepository
import com.aleksa.data.fake.fakeProductsDtoList
import com.aleksa.data.remote.ProductDto
import com.aleksa.data.source.NetworkProductRemoteDataSource
import com.aleksa.data.source.ProductRemoteDataSource
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
    fun provideFakeNetworkExecutor(
        @ApplicationContext appContext: Context,
    ): FakeNetworkExecutor {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
        val storageFile = File(appContext.filesDir, "fake_products.json")
        var productsJson = loadOrSeedProductsJson(storageFile, json)
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
}
