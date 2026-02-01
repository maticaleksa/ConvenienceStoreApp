package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.ProductRepository
import com.aleksa.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeProductRepository : ProductRepository {
    val products = MutableStateFlow<List<Product>>(emptyList())

    override fun observeAll(): Flow<List<Product>> = products

    override fun observeSearch(query: String): Flow<List<Product>> {
        val trimmed = query.trim()
        return products.map { list ->
            if (trimmed.isBlank()) {
                list
            } else {
                list.filter {
                    it.name.contains(trimmed, ignoreCase = true) ||
                        it.barcode.contains(trimmed, ignoreCase = true)
                }
            }
        }
    }

    override suspend fun upsert(product: Product) {
        val updated = products.value.toMutableList()
        val index = updated.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            updated[index] = product
        } else {
            updated.add(product)
        }
        products.value = updated
    }
}
