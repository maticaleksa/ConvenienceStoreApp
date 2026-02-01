package com.aleksa.conveniencestorestockmanagement.viewmodel

import com.aleksa.conveniencestorestockmanagement.MainDispatcherRule
import com.aleksa.conveniencestorestockmanagement.fakes.FakeDataCommandBus
import com.aleksa.conveniencestorestockmanagement.fakes.FakeTransactionRepository
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.data.repository.TransactionsSyncChannelKey
import com.aleksa.domain.event.TransactionDataCommand.RefreshAll
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.TransactionDateFilter
import com.aleksa.domain.usecases.TransactionFilterUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var filterUseCase: TransactionFilterUseCase
    private lateinit var dataCommandBus: FakeDataCommandBus
    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setUp() {
        transactionRepository = FakeTransactionRepository()
        filterUseCase = TransactionFilterUseCase()
        dataCommandBus = FakeDataCommandBus()
        syncCoordinator = SyncCoordinator()
        viewModel = TransactionsViewModel(
            transactionRepository = transactionRepository,
            filterUseCase = filterUseCase,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )
    }

    @Test
    fun `updates uiState when repository emits`() = runTest(dispatcher) {
        advanceUntilIdle()

        val t1 = transaction("t1", TransactionType.SALE)
        val t2 = transaction("t2", TransactionType.RESTOCK)
        transactionRepository.transactions.value = listOf(t1, t2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(t1, t2), state.items)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `updateSelectedTypes filters items`() = runTest(dispatcher) {
        val t1 = transaction("t1", TransactionType.SALE)
        val t2 = transaction("t2", TransactionType.RESTOCK)
        transactionRepository.transactions.value = listOf(t1, t2)
        advanceUntilIdle()

        viewModel.updateSelectedTypes(setOf(TransactionType.SALE))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(t1), state.items)
        assertEquals(setOf(TransactionType.SALE), state.selectedTypes)
    }

    @Test
    fun `updateDateFilter filters items`() = runTest(dispatcher) {
        val recent = transaction("t1", TransactionType.SALE, daysAgo = 1)
        val old = transaction("t2", TransactionType.SALE, daysAgo = 31)
        transactionRepository.transactions.value = listOf(recent, old)
        advanceUntilIdle()

        viewModel.updateDateFilter(TransactionDateFilter.LAST_30_DAYS)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(recent), state.items)
        assertEquals(TransactionDateFilter.LAST_30_DAYS, state.dateFilter)
    }

    @Test
    fun `refresh emits refresh command`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { dataCommandBus.events.first() }
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(RefreshAll, event.await())
    }

    @Test
    fun `syncing reflects channel active`() = runTest(dispatcher) {
        val syncChannel = syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)

        assertFalse(viewModel.uiState.value.isSyncing)
        val gate = CompletableDeferred<Unit>()
        val job = async {
            syncChannel.execute { gate.await() }
        }
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isSyncing)

        gate.complete(Unit)
        job.await()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isSyncing)
    }

    private fun transaction(
        id: String,
        type: TransactionType,
        daysAgo: Int = 0,
    ): Transaction {
        val now = Clock.System.now()
        val millisAgo = daysAgo * 24 * 60 * 60 * 1000L
        val date = kotlinx.datetime.Instant.fromEpochMilliseconds(
            now.toEpochMilliseconds() - millisAgo
        )
        return Transaction(
            id = id,
            date = date,
            type = type,
            productId = "p1",
            quantity = 1,
            notes = null,
        )
    }
}
