package com.aleksa.domain.usecases

import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Emits products whose stock level is at or below their minimum threshold.
 */
class LowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    /**
     * Observes products that are currently low on stock.
     *
     * @return A flow of products where current stock is below the minimum.
     */
    operator fun invoke(): Flow<List<Product>> =
        productRepository.observeAll().map { products ->
            products.filter { it.currentStockLevel <= it.minimumStockLevel }
        }
}
