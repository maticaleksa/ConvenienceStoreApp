package com.aleksa.domain

import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction

interface StockTransactionRepository {
    suspend fun applyTransaction(
        updatedProduct: Product,
        transaction: Transaction,
    ): StockTransactionResult
}
