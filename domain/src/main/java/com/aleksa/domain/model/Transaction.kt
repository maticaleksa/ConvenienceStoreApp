package com.aleksa.domain.model

import kotlinx.datetime.Instant

data class Transaction(
    val id: String,
    val date: Instant,
    val type: TransactionType,
    val productId: String,
    val quantity: Int,
    val notes: String?,
)
