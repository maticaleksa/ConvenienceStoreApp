package com.aleksa.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    /**
     * Observes all suppliers.
     */
    fun getAllFlow(): Flow<List<SupplierEntity>>

    @Query(
        """
        SELECT * FROM suppliers
        WHERE LOWER(name) LIKE :query
           OR LOWER(contactPerson) LIKE :query
           OR LOWER(phone) LIKE :query
           OR LOWER(email) LIKE :query
           OR LOWER(address) LIKE :query
        ORDER BY name ASC
        """
    )
    /**
     * Searches across common supplier fields using a case-insensitive LIKE query.
     *
     * Pass a wildcarded query string (for example "%term%").
     */
    fun searchFlow(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    /**
     * Loads all suppliers.
     */
    suspend fun getAll(): List<SupplierEntity>

    @Query("SELECT id FROM suppliers")
    /**
     * Returns all supplier ids.
     */
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    /**
     * Inserts suppliers and ignores conflicts.
     */
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    /**
     * Inserts a supplier and ignores conflicts.
     */
    suspend fun insert(supplier: SupplierEntity)

    @androidx.room.Update
    /**
     * Updates the provided suppliers.
     */
    suspend fun updateAll(suppliers: List<SupplierEntity>)

    @androidx.room.Update
    /**
     * Updates the provided supplier.
     */
    suspend fun update(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id IN (:ids)")
    /**
     * Deletes suppliers with ids in the provided collection.
     */
    suspend fun deleteByIds(ids: Collection<String>)
}
