package com.aleksa.data.source

import com.aleksa.data.database.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryDataSource {
    fun getAllFlow(): Flow<List<CategoryEntity>>
    suspend fun upsertAll(categories: List<CategoryEntity>)
}
