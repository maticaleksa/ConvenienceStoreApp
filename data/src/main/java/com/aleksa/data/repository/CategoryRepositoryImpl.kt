package com.aleksa.data.repository

import com.aleksa.data.database.CategoryEntity
import com.aleksa.data.source.CategoryDataSource
import com.aleksa.domain.CategoryRepository
import com.aleksa.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDataSource: CategoryDataSource
) : CategoryRepository {
    override fun observeAll(): Flow<List<Category>> {
        return categoryDataSource.getAllFlow().map { list ->
            list.map { it.toDomain() }
        }
    }
}

private fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name
)
