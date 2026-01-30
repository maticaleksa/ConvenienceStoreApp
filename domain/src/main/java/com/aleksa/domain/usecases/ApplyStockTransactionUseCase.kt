package com.aleksa.domain.usecases

import com.aleksa.domain.StockTransactionRepository
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

class ApplyStockTransactionUseCase @Inject constructor(
    private val repository: StockTransactionRepository,
) {
    suspend operator fun invoke(
        product: Product,
        quantity: Int,
        notes: String?,
        type: TransactionType,
    ) {
        val adjusted = when (type) {
            TransactionType.RESTOCK ->
                product.copy(currentStockLevel = product.currentStockLevel + quantity)
            TransactionType.SALE ->
                product.copy(currentStockLevel = product.currentStockLevel - quantity)
        }
        val transaction = Transaction(
            id = UUID.randomUUID().toString(),
            date = Clock.System.now(),
            type = type,
            productId = product.id,
            quantity = quantity,
            notes = notes?.trim().orEmpty().ifBlank { null },
        )
        repository.applyTransaction(adjusted, transaction)
    }
}
