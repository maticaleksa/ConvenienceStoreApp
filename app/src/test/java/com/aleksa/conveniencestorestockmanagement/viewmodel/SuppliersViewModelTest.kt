package com.aleksa.conveniencestorestockmanagement.viewmodel

import com.aleksa.conveniencestorestockmanagement.MainDispatcherRule
import com.aleksa.conveniencestorestockmanagement.fakes.FakeDataCommandBus
import com.aleksa.conveniencestorestockmanagement.fakes.FakeSupplierRepository
import com.aleksa.conveniencestorestockmanagement.fakes.RecordingSupplierRepository
import com.aleksa.conveniencestorestockmanagement.fakes.supplier
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.core.arch.sync.SyncCoordinator
import com.aleksa.core.arch.sync.UnknownSyncError
import com.aleksa.data.repository.SuppliersSyncChannelKey
import com.aleksa.domain.event.SupplierDataCommand.RefreshAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
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
class SuppliersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(dispatcher)

    private lateinit var supplierRepository: FakeSupplierRepository
    private lateinit var dataCommandBus: FakeDataCommandBus
    private lateinit var syncCoordinator: SyncCoordinator
    private lateinit var viewModel: SuppliersViewModel

    @Before
    fun setUp() {
        supplierRepository = FakeSupplierRepository()
        dataCommandBus = FakeDataCommandBus()
        syncCoordinator = SyncCoordinator()
        viewModel = SuppliersViewModel(
            supplierRepository = supplierRepository,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )
    }

    @Test
    fun `updates uiState when search query changes`() = runTest(dispatcher) {
        advanceUntilIdle()
        supplierRepository.suppliers.value = listOf(
            supplier("s1", "Acme"),
            supplier("s2", "Baker"),
        )

        viewModel.onSearchQueryChanged("acme")
        advanceTimeBy(301)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("s1"), state.items.map { it.id })
        assertFalse(state.isEmpty)
        assertTrue(state.isSearchActive)
        assertEquals("acme", state.searchQuery)
    }

    @Test
    fun `debounce delays search until 300ms elapsed`() = runTest(dispatcher) {
        advanceUntilIdle()
        supplierRepository.suppliers.value = listOf(
            supplier("s1", "Acme"),
            supplier("s2", "Baker"),
        )

        viewModel.onSearchQueryChanged("acme")
        advanceTimeBy(299)
        runCurrent()

        val before = viewModel.uiState.value
        assertEquals(listOf("s1", "s2"), before.items.map { it.id })

        advanceTimeBy(1)
        runCurrent()
        val after = viewModel.uiState.value
        assertEquals(listOf("s1"), after.items.map { it.id })
    }

    @Test
    fun `search ignores identical queries`() = runTest(dispatcher) {
        val recordingRepository = RecordingSupplierRepository()
        val viewModel = SuppliersViewModel(
            supplierRepository = recordingRepository,
            dataCommandBus = dataCommandBus,
            syncCoordinator = syncCoordinator,
        )

        viewModel.onSearchQueryChanged("acme")
        advanceTimeBy(301)
        advanceUntilIdle()
        viewModel.onSearchQueryChanged("acme")
        advanceTimeBy(301)
        advanceUntilIdle()

        assertEquals(listOf("acme"), recordingRepository.queries)
    }

    @Test
    fun `clearSearch resets search state`() = runTest(dispatcher) {
        viewModel.onSearchQueryChanged("acme")
        advanceTimeBy(301)
        advanceUntilIdle()
        viewModel.clearSearch()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertFalse(state.isSearchActive)
    }

    @Test
    fun `refresh emits refresh command`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            dataCommandBus.events.first()
        }
        viewModel.refresh()
        advanceUntilIdle()

        assertEquals(RefreshAll, event.await())
    }

    @Test
    fun `emits message when sync error occurs`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.first()
        }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)
            .reportError(UnknownSyncError("boom"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("boom"), event.await())
    }

    @Test
    fun `sync error falls back to throwable message`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.first()
        }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)
            .reportError(UnknownSyncError(null), IllegalStateException("from throwable"))
        advanceUntilIdle()

        assertEquals(UiEvent.Message("from throwable"), event.await())
    }

    @Test
    fun `sync error falls back to generic message`() = runTest(dispatcher) {
        val event = async(start = CoroutineStart.UNDISPATCHED) {
            viewModel.events.first()
        }
        advanceUntilIdle()
        syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)
            .reportError(UnknownSyncError(null), null)
        advanceUntilIdle()

        assertEquals(UiEvent.Message("Sync failed"), event.await())
    }

    @Test
    fun `syncing reflects channel active`() = runTest(dispatcher) {
        val syncChannel = syncCoordinator.getOrCreateChannel(SuppliersSyncChannelKey)

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

}
