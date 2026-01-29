package com.aleksa.domain.usecases

import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LowStockProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> =
        productRepository.observeAll().map { products ->
            products.filter { it.currentStockLevel <= it.minimumStockLevel }
        }
}
