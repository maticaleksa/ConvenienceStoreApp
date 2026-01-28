package com.aleksa.domain.model

import java.time.Instant

data class Transaction(
    val id: String,
    val date: Instant,
    val type: TransactionType,
    val productId: String,
    val quantity: Int,
    val notes: String?,
)

enum class TransactionType {
    RESTOCK,
    SALE,
}
