package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserEntity
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(identifier: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(identifier, pass)
            result.onSuccess {
                _authState.value = AuthState.Success
                onSuccess()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
        }
    }

    fun register(
        name: String,
        username: String,
        email: String,
        phone: String,
        pass: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(name, username, email, phone, pass)
            result.onSuccess {
                _authState.value = AuthState.Success
                onSuccess()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun updateProfile(name: String, username: String, bio: String, avatarUrl: String) {
        viewModelScope.launch {
            authRepository.updateProfile(name, username, bio, avatarUrl)
        }
    }

    fun toggleActiveStatus(isEnabled: Boolean) {
        viewModelScope.launch {
            authRepository.toggleActiveStatus(isEnabled)
        }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            _authState.value = AuthState.Idle
            onLoggedOut()
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String) : AuthState()
    }
}

class AuthViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
