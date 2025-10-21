package com.example.proyecto.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// State
data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)

// Events (User Actions)
sealed interface LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent
    data class PasswordChanged(val password: String) : LoginEvent
    object LoginClicked : LoginEvent
    object ErrorDismissed : LoginEvent
}

// ViewModel
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, error = null) }
            }

            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, error = null) }
            }

            is LoginEvent.LoginClicked -> {
                login()
            }

            is LoginEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun login() {
        val currentState = _state.value

        // Validation
        if (!validateEmailUseCase(currentState.email)) {
            _state.update { it.copy(error = "Correo inválido. Debe ser un correo UVG (@uvg.edu.gt)") }
            return
        }

        if (!validatePasswordUseCase(currentState.password)) {
            _state.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            loginUseCase(currentState.email, currentState.password)
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        isLoginSuccessful = true
                    ) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al iniciar sesión"
                    ) }
                }
        }
    }
}