package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import com.aleksa.conveniencestorestockmanagement.network.NetworkMonitor
import com.aleksa.conveniencestorestockmanagement.uistate.BaseUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class BaseViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
) : ViewModel() {
    val uiState: StateFlow<BaseUiState> = networkMonitor.isOnline
        .map { isOnline -> BaseUiState(isOnline = isOnline) }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            BaseUiState(isOnline = networkMonitor.isOnline.value),
        )
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()
}
