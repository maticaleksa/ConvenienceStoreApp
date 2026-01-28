package com.aleksa.data.source

import com.aleksa.data.database.CategoryEntity

interface CategoryDataSource {
    suspend fun upsertAll(categories: List<CategoryEntity>)
}
