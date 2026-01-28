package com.aleksa.conveniencestorestockmanagement.di

import android.content.Context
import androidx.room.Room
import com.aleksa.core.arch.event.DefaultDataCommandBus
import com.aleksa.core.arch.event.DataCommandBus
import com.aleksa.data.database.ProductDao
import com.aleksa.data.database.StockManagementDatabase
import com.aleksa.data.source.ProductDataSource
import com.aleksa.data.source.RoomProductDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): StockManagementDatabase {
        return Room.databaseBuilder(
            context,
            StockManagementDatabase::class.java,
            "stockmanagement.db",
        ).build()
    }

    @Provides
    fun provideProductDao(
        database: StockManagementDatabase,
    ): ProductDao = database.productDao()

    @Provides
    fun provideProductDataSource(
        dataSource: RoomProductDataSource,
    ): ProductDataSource = dataSource

    @Provides
    @Singleton
    fun provideDomainEventBus(): DataCommandBus = DefaultDataCommandBus()
}
