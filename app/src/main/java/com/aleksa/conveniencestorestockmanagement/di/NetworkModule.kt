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
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.api.ApiPaths
import com.aleksa.network.config.HttpClientFactory
import com.aleksa.network.config.NetworkConfig
import com.aleksa.network.fake.FakeNetworkExecutor
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

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

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
    fun provideFakeNetworkExecutor(): FakeNetworkExecutor {
        val productsJson =
            Json.encodeToString(ListSerializer(ProductDto.serializer()), fakeProductsDtoList)
        val firstProduct = fakeProductsDtoList.first()
        return FakeNetworkExecutor(
            routes = mapOf(
                "/greeting" to NetworkResult.Success("Hello from Fake Network"),
                "GET ${ApiPaths.PRODUCTS}" to NetworkResult.Success(productsJson),
                "POST ${ApiPaths.PRODUCTS}" to NetworkResult.Success(firstProduct),
            ),
        )
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
