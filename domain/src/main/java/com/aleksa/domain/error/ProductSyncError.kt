package com.aleksa.domain.error

import com.aleksa.core.arch.sync.SyncError

sealed interface ProductSyncError : SyncError {
    data class Network(
        override val message: String? = null,
        val code: String? = null,
        val details: Map<String, String>? = null
    ) : ProductSyncError

    data class Unknown(
        override val message: String? = null
    ) : ProductSyncError
}
