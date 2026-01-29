package com.aleksa.data.remote

import com.aleksa.domain.model.Transaction
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    val id: String,
    val dateEpochMillis: Long,
    val type: String,
    val productId: String,
    val quantity: Int,
    val notes: String?,
)

fun Transaction.toDto(): TransactionDto = TransactionDto(
    id = id,
    dateEpochMillis = date.toEpochMilliseconds(),
    type = type.name,
    productId = productId,
    quantity = quantity,
    notes = notes,
)
