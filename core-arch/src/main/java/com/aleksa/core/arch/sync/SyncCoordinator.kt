package com.aleksa.core.arch.sync

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncCoordinator @Inject constructor() {

    private val channels = ConcurrentHashMap<SyncChannelKey, SyncChannel>()

    fun getOrCreateChannel(key: SyncChannelKey): SyncChannel {
        return channels.computeIfAbsent(key) { SyncChannel() }
    }

    fun getChannel(key: SyncChannelKey): SyncChannel? {
        return channels[key]
    }

    fun isAnySyncActive(): Flow<Boolean> {
        val activeFlows = channels.values.toList().map { it.isActive }
        if (activeFlows.isEmpty()) return flowOf(false)
        return combine(activeFlows) { activeStates ->
            activeStates.any { it }
        }
    }
}
