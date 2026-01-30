package com.aleksa.domain

sealed class StockTransactionResult {
    object Success : StockTransactionResult()
    data class Error(val message: String) : StockTransactionResult()
}
