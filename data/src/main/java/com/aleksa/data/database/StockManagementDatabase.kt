package com.aleksa.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false,
)
abstract class StockManagementDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
}
