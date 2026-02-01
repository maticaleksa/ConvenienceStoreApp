package com.aleksa.conveniencestorestockmanagement.viewmodel

import com.aleksa.conveniencestorestockmanagement.MainDispatcherRule
import com.aleksa.conveniencestorestockmanagement.fakes.FakeProductRepository
import com.aleksa.conveniencestorestockmanagement.fakes.FakeStockTransactionRepository
import com.aleksa.conveniencestorestockmanagement.fakes.product
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.domain.StockTransactionResult
import com.aleksa.domain.model.Category
import com.aleksa.domain.usecases.ApplyStockTransactionUseCase
import com.aleksa.domain.usecases.ProductSearchUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockAddViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var productRepository: FakeProductRepository
    private lateinit var transactionRepository: FakeStockTransactionRepository
    private lateinit var applyUseCase: ApplyStockTransactionUseCase
    private lateinit var viewModel: StockAddViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        transactionRepository = FakeStockTransactionRepository()
        applyUseCase = ApplyStockTransactionUseCase(transactionRepository)
        viewModel = StockAddViewModel(
            productSearchUseCase = ProductSearchUseCase(productRepository),
            productRepository = productRepository,
            syncCoordinator = SyncCoordinator(),
            applyStockTransactionUseCase = applyUseCase,
        )
    }

    @Test
    fun `valid quantity and selection enables save`() = runTest(dispatcher) {
        val category = Category(id = "c1", name = "Dairy")
        productRepository.products.value = listOf(
            product("p1", "Milk", category, currentStockLevel = 5, minimumStockLevel = 2),
        )

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        viewModel.onProductSelected("Milk")
        viewModel.onQuantityChanged("3")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isQuantityValid)
        assertEquals(3, state.quantity)
        assertEquals("p1", state.selectedProductId)
    }

    @Test
    fun `save success resets quantity and notes`() = runTest(dispatcher) {
        val category = Category(id = "c1", name = "Dairy")
        productRepository.products.value = listOf(
            product("p1", "Milk", category, currentStockLevel = 5, minimumStockLevel = 2),
        )

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        viewModel.onProductSelected("Milk")
        viewModel.onQuantityChanged("2")
        viewModel.onNotesChanged("restock")
        advanceUntilIdle()

        viewModel.save()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.quantity)
        assertEquals("", state.notes)
    }

    @Test
    fun `save error emits message`() = runTest(dispatcher) {
        transactionRepository.result = StockTransactionResult.Error("failed")
        val category = Category(id = "c1", name = "Dairy")
        productRepository.products.value = listOf(
            product("p1", "Milk", category, currentStockLevel = 5, minimumStockLevel = 2),
        )

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        viewModel.onProductSelected("Milk")
        viewModel.onQuantityChanged("2")
        advanceUntilIdle()

        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        viewModel.save()
        advanceUntilIdle()

        assertEquals(UiEvent.Message("failed"), event.await())
    }

    @Test
    fun `quantity input clamps to non-negative`() = runTest(dispatcher) {
        viewModel.onSearchQueryChanged("")
        viewModel.onQuantityChanged("-5")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(0, state.quantity)
        assertFalse(state.isQuantityValid)
    }
}
