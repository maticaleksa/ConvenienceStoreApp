package com.aleksa.core.arch.sync


sealed class SyncState {
    object Idle : SyncState()
    object Loading : SyncState()
    data class Success(
        val itemCount: Int = 0,
        val syncedAt: Long = System.currentTimeMillis()
    ) : SyncState()
    data class Error(
        val error: SyncError,
        val throwable: Throwable? = null
    ) : SyncState()

    fun isLoading(): Boolean = this is Loading
    fun isError(): Boolean = this is Error
    fun isSuccess(): Boolean = this is Success
}
