package com.aleksa.domain

/**
 * Result of applying a stock transaction.
 */
sealed class StockTransactionResult {
    /**
     * Indicates the transaction was applied successfully.
     */
    object Success : StockTransactionResult()

    /**
     * Indicates the transaction failed with a human-readable message.
     */
    data class Error(val message: String) : StockTransactionResult()
}
