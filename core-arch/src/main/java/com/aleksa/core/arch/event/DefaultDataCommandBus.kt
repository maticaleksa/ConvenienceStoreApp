package com.aleksa.core.arch.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DefaultDataCommandBus : DataCommandBus {
    private val _events =
        MutableSharedFlow<DataCommand>(
            replay = 0,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    override val events: Flow<DataCommand> = _events.asSharedFlow()

    override suspend fun emit(event: DataCommand) {
        _events.emit(event)
    }
}
