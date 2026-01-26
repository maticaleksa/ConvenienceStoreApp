package com.aleksa.conveniencestorestockmanagement.di

import com.aleksa.conveniencestorestockmanagement.data.NetworkGreetingRepository
import com.aleksa.conveniencestorestockmanagement.domain.GreetingRepository
import com.aleksa.network.ErrorResponse
import com.aleksa.network.NetworkExecutor
import com.aleksa.network.NetworkResult
import com.aleksa.network.fake.FakeNetworkExecutor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFakeNetworkExecutor(): FakeNetworkExecutor {
        return FakeNetworkExecutor(
            routes = mapOf(
                "/greeting" to NetworkResult.Success("Hello from Fake Network"),
                "/greeting-error" to NetworkResult.Error(
                    ErrorResponse(message = "Fake error response"),
                ),
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
}
