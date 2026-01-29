package com.aleksa.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import com.aleksa.data.remote.TransactionDto
import kotlinx.datetime.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val type: TransactionType,
    val productId: String,
    val quantity: Int,
    val notes: String?,
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    date = Instant.fromEpochMilliseconds(dateEpochMillis),
    type = type,
    productId = productId,
    quantity = quantity,
    notes = notes,
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    dateEpochMillis = date.toEpochMilliseconds(),
    type = type,
    productId = productId,
    quantity = quantity,
    notes = notes,
)

fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    dateEpochMillis = dateEpochMillis,
    type = runCatching { TransactionType.valueOf(type) }.getOrElse { TransactionType.SALE },
    productId = productId,
    quantity = quantity,
    notes = notes,
)
