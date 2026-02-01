package com.aleksa.domain.usecases

import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ProductFilters(
    val categoryIds: Set<String> = emptySet()
)

/**
 * Provides a reactive product search with optional category filtering.
 */
class ProductSearchUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    /**
     * Observes products matching the query and optional filters.
     *
     * @param query Free-text search query; blank returns all products.
     * @param filters Optional category filtering rules. Category ids are trimmed and
     * filtered for non-blank values before being applied.
     * @return A flow of products filtered by query and category ids.
     */
    operator fun invoke(
        query: String,
        filters: ProductFilters = ProductFilters()
    ): Flow<List<Product>> {
        val base = if (query.isBlank()) {
            productRepository.observeAll()
        } else {
            productRepository.observeSearch(query)
        }

        val categoryIds = filters.categoryIds.map { it.trim() }.filter { it.isNotBlank() }.toSet()
        if (categoryIds.isEmpty()) return base

        return base.map { products ->
            products.filter { it.category.id in categoryIds }
        }
    }
}
