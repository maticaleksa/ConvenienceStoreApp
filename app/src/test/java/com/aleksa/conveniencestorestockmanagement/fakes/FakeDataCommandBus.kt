package com.aleksa.conveniencestorestockmanagement.fakes

import com.aleksa.core.arch.event.DataCommand
import com.aleksa.core.arch.event.DataCommandBus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeDataCommandBus : DataCommandBus {
    private val _events = MutableSharedFlow<DataCommand>()
    override val events: Flow<DataCommand> = _events

    override suspend fun emit(event: DataCommand) {
        _events.emit(event)
    }
}
