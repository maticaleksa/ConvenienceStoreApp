package com.aleksa.data.source

import com.aleksa.data.database.CategoryDao
import com.aleksa.data.database.CategoryEntity
import javax.inject.Inject

class RoomCategoryDataSource @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryDataSource {
    override suspend fun upsertAll(categories: List<CategoryEntity>) {
        categoryDao.upsertAll(categories)
    }
}
