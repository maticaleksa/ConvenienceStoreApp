package com.aleksa.domain.usecases

import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import kotlinx.datetime.TimeZone
import javax.inject.Inject

class TransactionFilterUseCase @Inject constructor() {
    operator fun invoke(
        items: List<Transaction>,
        types: Set<TransactionType>,
        dateFilter: TransactionDateFilter,
        now: Instant = Clock.System.now(),
    ): List<Transaction> {
        val filteredByType = if (types.isEmpty()) {
            items
        } else {
            items.filter { it.type in types }
        }

        val cutoff = when (dateFilter) {
            TransactionDateFilter.ALL -> null
            TransactionDateFilter.LAST_7_DAYS ->
                now.minus(7, DateTimeUnit.DAY, TimeZone.UTC)
            TransactionDateFilter.LAST_30_DAYS ->
                now.minus(30, DateTimeUnit.DAY, TimeZone.UTC)
        }

        return if (cutoff == null) {
            filteredByType
        } else {
            filteredByType.filter { it.date >= cutoff }
        }
    }
}
