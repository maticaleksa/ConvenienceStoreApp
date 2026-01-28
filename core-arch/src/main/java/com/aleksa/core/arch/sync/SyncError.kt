package com.aleksa.core.arch.sync

interface SyncError {
    val message: String?
}

data class UnknownSyncError(
    override val message: String? = null
) : SyncError
