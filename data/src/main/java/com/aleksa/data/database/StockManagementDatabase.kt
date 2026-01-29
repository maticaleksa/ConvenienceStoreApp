package com.aleksa.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class, CategoryEntity::class, SupplierEntity::class, TransactionEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class StockManagementDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun supplierDao(): SupplierDao
    abstract fun transactionDao(): TransactionDao
}
