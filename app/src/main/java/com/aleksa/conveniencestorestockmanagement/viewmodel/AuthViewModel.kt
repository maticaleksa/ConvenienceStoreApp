package com.aleksa.conveniencestorestockmanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aleksa.conveniencestorestockmanagement.auth.AuthManager
import com.aleksa.conveniencestorestockmanagement.domain.AuthRepository
import com.aleksa.conveniencestorestockmanagement.domain.GetStartDestinationUseCase
import com.aleksa.conveniencestorestockmanagement.navigation.StartDestination
import com.aleksa.network.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager,
    private val getStartDestinationUseCase: GetStartDestinationUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    val isAuthenticated: StateFlow<Boolean> = authManager.isAuthenticated
    val startDestination: StateFlow<StartDestination> =
        authManager.isAuthenticated
            .map { isAuthenticated -> getStartDestinationUseCase(isAuthenticated) }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                getStartDestinationUseCase(authManager.isAuthenticated.value),
            )
    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    fun onLogin(username: String, password: String) {
        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is NetworkResult.Success -> {
                    if (result.data) {
                        authManager.setAuthenticated(true)
                    } else {
                        _loginError.value = "Username or password is incorrect"
                        authManager.setAuthenticated(false)
                    }
                }
                is NetworkResult.Error -> {
                    _loginError.value = result.error.message ?: "Login failed"
                    authManager.setAuthenticated(false)
                }
            }
        }
    }

    fun clearLoginError() {
        _loginError.value = null
    }

    fun onLogout() {
        authManager.setAuthenticated(false)
    }
}
