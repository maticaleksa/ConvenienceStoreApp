package com.aleksa.conveniencestorestockmanagement

import android.content.Context
import androidx.startup.Initializer
import com.aleksa.domain.ProductRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class RepositoryInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            RepositoryInitializerEntryPoint::class.java
        )

        // init repositories
        entryPoint.productRepository()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
@EntryPoint
@InstallIn(SingletonComponent::class)
interface RepositoryInitializerEntryPoint {
    fun productRepository(): ProductRepository
}
