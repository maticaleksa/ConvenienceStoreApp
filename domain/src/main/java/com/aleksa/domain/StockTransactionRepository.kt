package com.aleksa.domain

import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction

/**
 * Atomic operation for applying a stock change and recording its transaction.
 */
interface StockTransactionRepository {
    /**
     * Applies the stock update and persists the transaction as a single operation.
     *
     * @param updatedProduct The product with the new stock level.
     * @param transaction The transaction describing the change.
     * @return The result containing the persisted records.
     */
    suspend fun applyTransaction(
        updatedProduct: Product,
        transaction: Transaction,
    ): StockTransactionResult
}
