package com.aleksa.data.repository

import androidx.room.withTransaction
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.data.database.StockManagementDatabase
import com.aleksa.data.database.toEntity
import com.aleksa.data.remote.toDto
import com.aleksa.data.source.ProductDataSource
import com.aleksa.data.source.ProductRemoteDataSource
import com.aleksa.data.source.TransactionDataSource
import com.aleksa.data.source.TransactionRemoteDataSource
import com.aleksa.domain.StockTransactionRepository
import com.aleksa.domain.model.Product
import com.aleksa.domain.model.Transaction
import com.aleksa.network.NetworkResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockTransactionRepositoryImpl @Inject constructor(
    private val productRemoteDataSource: ProductRemoteDataSource,
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val productDataSource: ProductDataSource,
    private val transactionDataSource: TransactionDataSource,
    private val database: StockManagementDatabase,
    syncCoordinator: SyncCoordinator,
) : StockTransactionRepository {

    private val syncChannel = syncCoordinator.getOrCreateChannel(StockTransactionsSyncChannelKey)

    override suspend fun applyTransaction(updatedProduct: Product, transaction: Transaction) {
        syncChannel.execute {
            val productResult = productRemoteDataSource.upsert(updatedProduct.toDto())
            if (productResult is NetworkResult.Error) {
                throw IllegalStateException(
                    "Product remote update failed: ${productResult.error.message}"
                )
            }
            val transactionResult = transactionRemoteDataSource.upsert(transaction.toDto())
            if (transactionResult is NetworkResult.Error) {
                throw IllegalStateException(
                    "Transaction remote update failed: ${transactionResult.error.message}"
                )
            }
            database.withTransaction {
                productDataSource.upsert(updatedProduct.toEntity())
                transactionDataSource.upsert(transaction.toEntity())
            }
        }
    }
}
