package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.auth.AuthManager
import com.aleksa.conveniencestorestockmanagement.domain.AuthRepository
import com.aleksa.conveniencestorestockmanagement.domain.GetStartDestinationUseCase
import com.aleksa.conveniencestorestockmanagement.uistate.AuthUiState
import com.aleksa.conveniencestorestockmanagement.uistate.UiEvent
import com.aleksa.network.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val getStartDestinationUseCase: GetStartDestinationUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = authManager.isAuthenticated
        .map { isAuthenticated ->
            AuthUiState(
                isAuthenticated = isAuthenticated,
                startDestination = getStartDestinationUseCase(isAuthenticated),
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AuthUiState(
                isAuthenticated = authManager.isAuthenticated.value,
                startDestination = getStartDestinationUseCase(authManager.isAuthenticated.value),
            ),
        )
    val uiState: StateFlow<AuthUiState> = _uiState
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val events = _events.asSharedFlow()

    fun onLogin(username: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> {
                    if (result.data) {
                        authManager.setAuthenticated(true)
                    } else {
                        _events.tryEmit(UiEvent.Message("Username or password is incorrect"))
                        authManager.setAuthenticated(false)
                    }
                }
                is NetworkResult.Error -> {
                    _events.tryEmit(UiEvent.Message(result.error.message ?: "Login failed"))
                    authManager.setAuthenticated(false)
                }
            }
        }
    }

    fun onLogout() {
        authManager.setAuthenticated(false)
    }
}
