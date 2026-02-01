package com.aleksa.conveniencestorestockmanagement.viewmodel

import com.aleksa.conveniencestorestockmanagement.MainDispatcherRule
import com.aleksa.conveniencestorestockmanagement.fakes.product
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.conveniencestorestockmanagement.fakes.FakeDataCommandBus
import com.aleksa.conveniencestorestockmanagement.fakes.FakeProductRepository
import com.aleksa.conveniencestorestockmanagement.fakes.FakeTransactionRepository
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.UnknownSyncError
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.data.repository.TransactionsSyncChannelKey
import com.aleksa.domain.event.ProductDataCommand.RefreshAll as RefreshProducts
import com.aleksa.domain.event.TransactionDataCommand.RefreshAll as RefreshTransactions
import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Transaction
import com.aleksa.domain.model.TransactionType
import com.aleksa.domain.usecases.LowStockProductsUseCase
import com.aleksa.domain.usecases.RecentTransactionsUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var productRepository: FakeProductRepository
    private lateinit var transactionRepository: FakeTransactionRepository
    private lateinit var lowStockUseCase: LowStockProductsUseCase
    private lateinit var recentTransactionsUseCase: RecentTransactionsUseCase
    private lateinit var dataCommandBus: FakeDataCommandBus
    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        transactionRepository = FakeTransactionRepository()
        lowStockUseCase = LowStockProductsUseCase(productRepository)
        recentTransactionsUseCase = RecentTransactionsUseCase(transactionRepository)
        dataCommandBus = FakeDataCommandBus()
        syncCoordinator = SyncCoordinator()
        viewModel = DashboardViewModel(
            lowStockProductsUseCase = lowStockUseCase,
            recentTransactionsUseCase = recentTransactionsUseCase,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )
    }

    @Test
    fun `low stock updates uiState`() = runTest(dispatcher) {
        advanceUntilIdle()

        val category = Category(id = "c1", name = "Dairy")
        val items = listOf(
            product("p1", "Milk", category, currentStockLevel = 1, minimumStockLevel = 2),
            product("p2", "Bread", category, currentStockLevel = 0, minimumStockLevel = 3),
        )
        productRepository.products.value = items
        advanceUntilIdle()

        assertEquals(items, viewModel.uiState.value.lowStock)
    }

    @Test
    fun `recent transactions update uiState`() = runTest(dispatcher) {
        advanceUntilIdle()

        val transactions = listOf(
            transaction("t1"),
            transaction("t2"),
        )
        transactionRepository.transactions.value = transactions
        advanceUntilIdle()

        assertEquals(transactions, viewModel.uiState.value.recentTransactions)
    }

    @Test
    fun `refresh emits product and transaction refresh commands`() = runTest(dispatcher) {
        val events = async(start = CoroutineStart.UNDISPATCHED) {
            dataCommandBus.events.take(2).toList()
        }
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(listOf(RefreshProducts, RefreshTransactions), events.await())
    }

    @Test
    fun `sync error emits message`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
            .reportError(UnknownSyncError("boom"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("boom"), event.await())
    }

    @Test
    fun `sync error falls back to throwable message`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)
            .reportError(UnknownSyncError(null), IllegalStateException("from throwable"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("from throwable"), event.await())
    }

    @Test
    fun `sync error falls back to generic message`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)
            .reportError(UnknownSyncError(null), null)
        advanceUntilIdle()

        assertEquals(UiEvent.Message("Sync failed"), event.await())
    }

    @Test
    fun `syncing reflects either channel active`() = runTest(dispatcher) {
        val productsChannel = syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
        val transactionsChannel = syncCoordinator.getOrCreateChannel(TransactionsSyncChannelKey)
        val productsGate = CompletableDeferred<Unit>()
        val transactionsGate = CompletableDeferred<Unit>()

        assertFalse(viewModel.uiState.value.isSyncing)
        val productsJob = async {
            productsChannel.execute { productsGate.await() }
        }
        val transactionsJob = async {
            transactionsChannel.execute { transactionsGate.await() }
        }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSyncing)
        productsGate.complete(Unit)
        transactionsGate.complete(Unit)
        productsJob.await()
        transactionsJob.await()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isSyncing)
    }

    @Test
    fun `toggle flags update uiState`() = runTest(dispatcher) {
        assertFalse(viewModel.uiState.value.lowStockExpanded)
        assertFalse(viewModel.uiState.value.recentExpanded)

        viewModel.toggleLowStockExpanded()
        viewModel.toggleRecentExpanded()

        assertTrue(viewModel.uiState.value.lowStockExpanded)
        assertTrue(viewModel.uiState.value.recentExpanded)
    }

    private fun transaction(
        id: String,
        date: Instant = Clock.System.now(),
    ): Transaction = Transaction(
        id = id,
        date = date,
        type = TransactionType.SALE,
        productId = "p1",
        quantity = 1,
        notes = null,
    )

}
