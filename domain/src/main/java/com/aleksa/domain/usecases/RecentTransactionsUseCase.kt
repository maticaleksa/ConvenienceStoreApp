package com.aleksa.domain.usecases

import com.aleksa.domain.TransactionRepository
import com.aleksa.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import javax.inject.Inject

class RecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    operator fun invoke(days: Int): Flow<List<Transaction>> {
        val safeDays = days.coerceAtLeast(0)
        return transactionRepository.observeAll()
            .map { items ->
                if (safeDays == 0) {
                    emptyList()
                } else {
                    val cutoff = Clock.System.now()
                        .minus(safeDays, DateTimeUnit.DAY, TimeZone.UTC)
                    items.filter { it.date >= cutoff }
                }
            }
    }
}
