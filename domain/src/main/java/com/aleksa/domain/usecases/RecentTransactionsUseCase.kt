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

/**
 * Provides a reactive list of transactions within a recent time window.
 */
class RecentTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
) {
    /**
     * Observes transactions from the last [days] days.
     *
     * @param days Number of days to include; non-positive yields an empty list.
     * The cutoff is computed in UTC and includes transactions on or after it.
     * @return A flow of transactions within the computed range.
     */
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
