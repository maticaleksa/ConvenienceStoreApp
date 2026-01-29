package com.aleksa.data.fake

import com.aleksa.data.remote.TransactionDto
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import kotlinx.datetime.Instant

val fakeTransactionsList = listOf(
    Transaction(
        id = "t1",
        date = Instant.parse("2025-01-10T09:15:00Z"),
        type = TransactionType.RESTOCK,
        productId = "p1",
        quantity = 20,
        notes = "Weekly dairy restock",
    ),
    Transaction(
        id = "t2",
        date = Instant.parse("2025-01-11T12:40:00Z"),
        type = TransactionType.SALE,
        productId = "p4",
        quantity = 3,
        notes = "Lunch rush",
    ),
    Transaction(
        id = "t3",
        date = Instant.parse("2025-01-12T08:05:00Z"),
        type = TransactionType.RESTOCK,
        productId = "p2",
        quantity = 15,
        notes = "Bakery delivery",
    ),
    Transaction(
        id = "t4",
        date = Instant.parse("2025-01-12T18:22:00Z"),
        type = TransactionType.SALE,
        productId = "p5",
        quantity = 8,
        notes = "Evening snacks",
    ),
    Transaction(
        id = "t5",
        date = Instant.parse("2025-01-13T07:30:00Z"),
        type = TransactionType.RESTOCK,
        productId = "p6",
        quantity = 25,
        notes = "Pantry staples",
    ),
    Transaction(
        id = "t6",
        date = Instant.parse("2025-01-13T15:10:00Z"),
        type = TransactionType.SALE,
        productId = "p3",
        quantity = 5,
        notes = "Afternoon sales",
    ),
)

val fakeTransactionsDtoList =
    fakeTransactionsList.map {
        TransactionDto(
            id = it.id,
            dateEpochMillis = it.date.toEpochMilliseconds(),
            type = it.type.name,
            productId = it.productId,
            quantity = it.quantity,
            notes = it.notes,
        )
    }
