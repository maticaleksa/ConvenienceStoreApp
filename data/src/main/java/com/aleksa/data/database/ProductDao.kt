package com.aleksa.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAll(): List<ProductWithCategory>

    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllFlow(): Flow<List<ProductWithCategory>>

    @Transaction
    @Query(
        """
        SELECT * FROM products
        WHERE LOWER(name) LIKE :query
           OR LOWER(barcode) LIKE :query
           OR categoryId IN (SELECT id FROM categories WHERE LOWER(name) LIKE :query)
        ORDER BY name ASC
        """
    )
    fun searchFlow(query: String): Flow<List<ProductWithCategory>>

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Query("SELECT id FROM products")
    suspend fun getAllIds(): List<String>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductWithCategory?

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: String): Flow<ProductWithCategory?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(product: ProductEntity)

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Collection<String>)
}
