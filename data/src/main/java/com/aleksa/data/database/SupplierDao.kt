package com.aleksa.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
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
    fun searchFlow(query: String): Flow<List<SupplierEntity>>

    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    suspend fun getAll(): List<SupplierEntity>

    @Query("SELECT id FROM suppliers")
    suspend fun getAllIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(suppliers: List<SupplierEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(supplier: SupplierEntity)

    @androidx.room.Update
    suspend fun updateAll(suppliers: List<SupplierEntity>)

    @androidx.room.Update
    suspend fun update(supplier: SupplierEntity)

    @Query("DELETE FROM suppliers WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: Collection<String>)
}
