package com.aleksa.conveniencestorestockmanagement.uistate

import com.aleksa.conveniencestorestockmanagement.navigation.StartDestination

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val startDestination: StartDestination = StartDestination.Auth,
)
