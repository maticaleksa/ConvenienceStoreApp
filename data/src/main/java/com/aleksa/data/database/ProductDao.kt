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
    /**
     * Loads all products with related category and supplier data.
     */
    suspend fun getAll(): List<ProductWithCategorySupplier>

    @Transaction
    @Query("SELECT * FROM products ORDER BY name ASC")
    /**
     * Observes all products with related category and supplier data.
     */
    fun getAllFlow(): Flow<List<ProductWithCategorySupplier>>

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
    /**
     * Searches by name, barcode, or category name using a case-insensitive LIKE query.
     *
     * Pass a wildcarded query string (for example "%term%").
     */
    fun searchFlow(query: String): Flow<List<ProductWithCategorySupplier>>

    @Query("SELECT COUNT(*) FROM products")
    /**
     * Returns the total number of products.
     */
    suspend fun count(): Int

    @Query("SELECT id FROM products")
    /**
     * Returns all product ids.
     */
    suspend fun getAllIds(): List<String>

    @Query("SELECT DISTINCT supplierId FROM products")
    /**
     * Returns distinct supplier ids referenced by products.
     */
    suspend fun getSupplierIdsInUse(): List<String>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    /**
     * Loads a product by id with related category and supplier data.
     */
    suspend fun getById(id: String): ProductWithCategorySupplier?

    @Transaction
    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    /**
     * Observes a product by id with related category and supplier data.
     */
    fun getByIdFlow(id: String): Flow<ProductWithCategorySupplier?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    /**
     * Inserts or replaces the provided products.
     */
    suspend fun upsertAll(products: List<ProductEntity>)

    @Query("DELETE FROM products")
    /**
     * Deletes all products.
     */
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    /**
     * Inserts or replaces the provided product.
     */
    suspend fun upsert(product: ProductEntity)

    @Update
    /**
     * Updates the provided product.
     */
    suspend fun update(product: ProductEntity)

    @Delete
    /**
     * Deletes the provided product.
     */
    suspend fun delete(product: ProductEntity)

    @Query("DELETE FROM products WHERE id IN (:ids)")
    /**
     * Deletes products with ids in the provided collection.
     */
    suspend fun deleteByIds(ids: Collection<String>)
}
