package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.domain.TransactionRepository
import com.aleksa.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTransactionRepository : TransactionRepository {
    val transactions = MutableStateFlow<List<Transaction>>(emptyList())

    override fun observeAll(): Flow<List<Transaction>> = transactions

    override suspend fun upsert(transaction: Transaction) {
        val updated = transactions.value.toMutableList()
        val index = updated.indexOfFirst { it.id == transaction.id }
        if (index >= 0) {
            updated[index] = transaction
        } else {
            updated.add(transaction)
        }
        transactions.value = updated
    }
}
