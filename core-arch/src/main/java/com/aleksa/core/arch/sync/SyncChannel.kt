package com.aleksa.core.arch.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

class SyncChannel {

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    val state: StateFlow<SyncState> = _state.asStateFlow()

    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()

    private val syncLock = AtomicBoolean(false)

    suspend fun execute(block: suspend () -> Unit) {
        // Prevent concurrent syncs
        if (!syncLock.compareAndSet(false, true)) {
            return // Already syncing
        }

        _state.value = SyncState.Loading
        _isActive.value = true

        try {
            block()

            if (_state.value is SyncState.Loading) {
                _state.value = SyncState.Success(
                    syncedAt = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            reportError(UnknownSyncError(e.message), e)
        } finally {
            syncLock.set(false)
            _isActive.value = false
        }
    }

    fun reportError(error: SyncError, throwable: Throwable? = null) {
        _state.value = SyncState.Error(
            error = error,
            throwable = throwable
        )
    }
}
