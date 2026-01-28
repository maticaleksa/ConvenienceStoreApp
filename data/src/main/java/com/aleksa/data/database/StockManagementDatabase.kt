package com.aleksa.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class StockManagementDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
