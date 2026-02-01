package com.aleksa.domain.usecases

import com.aleksa.domain.StockTransactionRepository
import com.aleksa.domain.StockTransactionResult
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import kotlinx.datetime.Clock
import java.util.UUID
import javax.inject.Inject

/**
 * Applies a stock transaction by adjusting the product's stock level and persisting
 * the transaction through the repository.
 *
 * Restocks increase stock, sales decrease it, and the transaction is timestamped
 * with a new UUID.
 */
class ApplyStockTransactionUseCase @Inject constructor(
    private val repository: StockTransactionRepository,
) {
    /**
     * Applies a restock or sale transaction for the given product.
     *
     * @param product The product being updated.
     * @param quantity The number of units in the transaction.
     * @param notes Optional free-form notes; trimmed and stored as null when blank.
     * @param type The transaction type (restock or sale).
     * @return The repository result containing the updated product and transaction.
     */
    suspend operator fun invoke(
        product: Product,
        quantity: Int,
        notes: String?,
        type: TransactionType,
    ): StockTransactionResult {
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
        return repository.applyTransaction(adjusted, transaction)
    }
}
