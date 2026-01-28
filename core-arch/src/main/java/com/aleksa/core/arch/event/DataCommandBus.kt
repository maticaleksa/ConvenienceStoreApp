package com.aleksa.core.arch.event

import kotlinx.coroutines.flow.Flow

interface DataCommandBus {
    val events: Flow<DataCommand>
    suspend fun emit(event: DataCommand)
}
