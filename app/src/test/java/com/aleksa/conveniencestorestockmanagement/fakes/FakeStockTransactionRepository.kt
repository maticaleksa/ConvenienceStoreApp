package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.StockTransactionRepository
import com.aleksa.domain.StockTransactionResult
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction

class FakeStockTransactionRepository(
    var result: StockTransactionResult = StockTransactionResult.Success,
) : StockTransactionRepository {
    var lastProduct: Product? = null
    var lastTransaction: Transaction? = null

    override suspend fun applyTransaction(
        updatedProduct: Product,
        transaction: Transaction,
    ): StockTransactionResult {
        lastProduct = updatedProduct
        lastTransaction = transaction
        return result
    }
}
