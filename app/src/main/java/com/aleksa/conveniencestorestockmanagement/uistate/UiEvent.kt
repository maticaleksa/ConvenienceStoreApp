package com.aleksa.conveniencestorestockmanagement.uistate

sealed class UiEvent {
    data class Message(val text: String) : UiEvent()
    data object NavigateBack : UiEvent()
}
