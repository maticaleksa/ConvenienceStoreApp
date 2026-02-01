package com.aleksa.domain

import com.aleksa.domain.model.Category
import kotlinx.coroutines.flow.Flow

/**
 * Access to category data sources.
 */
interface CategoryRepository {
    /**
     * Observes all categories.
     */
    fun observeAll(): Flow<List<Category>>
}
