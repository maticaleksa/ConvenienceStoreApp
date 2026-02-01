package com.aleksa.conveniencestorestockmanagement.viewmodel

import com.aleksa.conveniencestorestockmanagement.MainDispatcherRule
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.UnknownSyncError
import com.aleksa.data.repository.ProductsSyncChannelKey
import com.aleksa.conveniencestorestockmanagement.fakes.FakeCategoryRepository
import com.aleksa.conveniencestorestockmanagement.fakes.FakeDataCommandBus
import com.aleksa.conveniencestorestockmanagement.fakes.FakeProductRepository
import com.aleksa.conveniencestorestockmanagement.fakes.product
import com.aleksa.domain.ProductRepository
import com.aleksa.domain.event.ProductDataCommand.RefreshAll
import com.aleksa.domain.model.Category
import com.aleksa.domain.model.Product
import com.aleksa.domain.usecases.ProductSearchUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var productRepository: FakeProductRepository
    private lateinit var categoryRepository: FakeCategoryRepository
    private lateinit var dataCommandBus: FakeDataCommandBus
    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var useCase: ProductSearchUseCase
    private lateinit var viewModel: ProductsViewModel

    @Before
    fun setUp() {
        productRepository = FakeProductRepository()
        categoryRepository = FakeCategoryRepository()
        dataCommandBus = FakeDataCommandBus()
        syncCoordinator = SyncCoordinator()
        useCase = ProductSearchUseCase(productRepository)
        viewModel = ProductsViewModel(
            productSearchUseCase = useCase,
            categoryRepository = categoryRepository,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )
    }

    @Test
    fun `updates uiState when search query changes`() = runTest(dispatcher) {
        advanceUntilIdle()
        val category = Category(id = "c1", name = "Dairy")
        productRepository.products.value = listOf(
            product("p1", "Milk", category),
            product("p2", "Bread", category),
        )

        viewModel.onSearchQueryChanged("milk")
        advanceTimeBy(301)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("p1"), state.items.map { it.id })
        assertFalse(state.isEmpty)
        assertTrue(state.isSearchActive)
        assertEquals("milk", state.searchQuery)
    }

    @Test
    fun `filters by selected categories`() = runTest(dispatcher) {
        advanceUntilIdle()
        val dairy = Category(id = "c1", name = "Dairy")
        val produce = Category(id = "c2", name = "Produce")
        productRepository.products.value = listOf(
            product("p1", "Milk", dairy),
            product("p2", "Apple", produce),
        )

        viewModel.onSearchQueryChanged("")
        advanceTimeBy(301)
        advanceUntilIdle()

        viewModel.updateSelectedCategories(setOf("c2"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("p2"), state.items.map { it.id })
        assertEquals(setOf("c2"), state.selectedCategoryIds)
    }

    @Test
    fun `refresh emits refresh command`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { dataCommandBus.events.first() }
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(RefreshAll, event.await())
    }

    @Test
    fun `emits message when sync error occurs`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
            .reportError(UnknownSyncError("boom"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("boom"), event.await())
    }

    @Test
    fun `sync error prefers throwable message when error message is null`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
            .reportError(UnknownSyncError(null), IllegalStateException("from throwable"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("from throwable"), event.await())
    }

    @Test
    fun `sync error falls back to generic message when no error info`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.events.first() }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(ProductsSyncChannelKey)
            .reportError(UnknownSyncError(null), null)
        advanceUntilIdle()

        assertEquals(UiEvent.Message("Sync failed"), event.await())
    }

    @Test
    fun `clearSearch resets search state`() = runTest(dispatcher) {
        viewModel.onSearchQueryChanged("milk")
        advanceTimeBy(301)
        advanceUntilIdle()
        viewModel.clearSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearchActive)
    }

    @Test
    fun `debounce delays search until 300ms elapsed`() = runTest(dispatcher) {
        advanceUntilIdle()
        val category = Category(id = "c1", name = "Dairy")
        productRepository.products.value = listOf(
            product("p1", "Milk", category),
            product("p2", "Bread", category),
        )

        viewModel.onSearchQueryChanged("milk")
        advanceTimeBy(299)
        runCurrent()

        val before = viewModel.uiState.value
        assertEquals(listOf("p1", "p2"), before.items.map { it.id })

        advanceTimeBy(1)
        runCurrent()
        val after = viewModel.uiState.value
        assertEquals(listOf("p1"), after.items.map { it.id })
    }

    @Test
    fun `search query and category changes re-filter results`() = runTest(dispatcher) {
        advanceUntilIdle()
        val dairy = Category(id = "c1", name = "Dairy")
        val produce = Category(id = "c2", name = "Produce")
        productRepository.products.value = listOf(
            product("p1", "Milk", dairy),
            product("p2", "Apple", produce),
            product("p3", "Yogurt", dairy),
        )

        viewModel.onSearchQueryChanged("m")
        advanceTimeBy(301)
        advanceUntilIdle()
        assertEquals(listOf("p1"), viewModel.uiState.value.items.map { it.id })

        viewModel.updateSelectedCategories(setOf("c1"))
        advanceUntilIdle()
        assertEquals(listOf("p1"), viewModel.uiState.value.items.map { it.id })

        viewModel.updateSelectedCategories(setOf("c2"))
        advanceUntilIdle()
        assertEquals(emptyList<String>(), viewModel.uiState.value.items.map { it.id })

        viewModel.onSearchQueryChanged("")
        advanceTimeBy(301)
        advanceUntilIdle()
        assertEquals(listOf("p2"), viewModel.uiState.value.items.map { it.id })
    }

    @Test
    fun `categories observation updates uiState categories`() = runTest(dispatcher) {
        val categories = listOf(
            Category(id = "c1", name = "Dairy"),
            Category(id = "c2", name = "Produce"),
        )
        categoryRepository.categories.value = categories
        advanceUntilIdle()

        assertEquals(categories, viewModel.uiState.value.categories)
    }

    @Test
    fun `search ignores identical queries`() = runTest(dispatcher) {
        val categoryRepository = FakeCategoryRepository()
        val dataCommandBus = FakeDataCommandBus()
        val syncCoordinator = SyncCoordinator()
        val recordingRepository = RecordingProductRepository()
        val useCase = ProductSearchUseCase(recordingRepository)
        val viewModel = ProductsViewModel(
            productSearchUseCase = useCase,
            categoryRepository = categoryRepository,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )

        viewModel.onSearchQueryChanged("milk")
        advanceTimeBy(301)
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("milk")
        advanceTimeBy(301)
        advanceUntilIdle()

        assertEquals(listOf("milk"), recordingRepository.queries)
    }

    @Test
    fun `initial loading becomes false after first emission`() = runTest(dispatcher) {
        val initial = viewModel.uiState.value
        assertFalse(initial.isLoading)
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `addProductEvents emits when add clicked`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) { viewModel.addProductEvents.first() }
        viewModel.onAddProductClicked()
        advanceUntilIdle()

        assertEquals(Unit, event.await())
    }

    private class RecordingProductRepository : ProductRepository {
        val queries = mutableListOf<String>()
        private val products = MutableStateFlow<List<Product>>(emptyList())

        override fun observeAll(): Flow<List<Product>> = products

        override fun observeSearch(query: String): Flow<List<Product>> {
            queries.add(query.trim())
            val trimmed = query.trim()
            return products.map { list ->
                if (trimmed.isBlank()) {
                    list
                } else {
                    list.filter {
                        it.name.contains(trimmed, ignoreCase = true) ||
                            it.barcode.contains(trimmed, ignoreCase = true)
                    }
                }
            }
        }

        override suspend fun upsert(product: Product) = Unit
    }

}
