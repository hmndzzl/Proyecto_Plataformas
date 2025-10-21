package com.example.proyecto.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.*
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// State
data class ProfileState(
    val user: User? = null,
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val showLogoutDialog: Boolean = false,
    val isLogoutSuccessful: Boolean = false
)

// Events
sealed interface ProfileEvent {
    object LoadProfile : ProfileEvent
    object LogoutClicked : ProfileEvent
    object LogoutConfirmed : ProfileEvent
    object LogoutCancelled : ProfileEvent
    data class ReservationClicked(val reservationId: String) : ProfileEvent
    data class CancelReservation(val reservationId: String) : ProfileEvent
    object ErrorDismissed : ProfileEvent
}

// ViewModel
class ProfileViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserReservationsUseCase: GetUserReservationsUseCase,
    private val cancelReservationUseCase: CancelReservationUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.LoadProfile -> loadProfile()

            is ProfileEvent.LogoutClicked -> {
                _state.update { it.copy(showLogoutDialog = true) }
            }

            is ProfileEvent.LogoutConfirmed -> logout()

            is ProfileEvent.LogoutCancelled -> {
                _state.update { it.copy(showLogoutDialog = false) }
            }

            is ProfileEvent.ReservationClicked -> {
                // Navigation handled by UI
            }

            is ProfileEvent.CancelReservation -> {
                cancelReservation(event.reservationId)
            }

            is ProfileEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val user = getCurrentUserUseCase()

                if (user == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    ) }
                    return@launch
                }

                // Sync reservations
                getUserReservationsUseCase.sync(user.id)

                _state.update { it.copy(
                    user = user,
                    isLoading = false
                ) }

                // Observe reservations
                observeReservations(user.id)

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar perfil"
                ) }
            }
        }
    }

    private fun observeReservations(userId: String) {
        viewModelScope.launch {
            getUserReservationsUseCase(userId).collect { reservations ->
                _state.update { it.copy(reservations = reservations) }
            }
        }
    }

    private fun cancelReservation(reservationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            cancelReservationUseCase(reservationId)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    // Reservations will update via Flow
                }
                .onFailure { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cancelar reserva"
                    ) }
                }
        }
    }

    private fun logout() {
        viewModelScope.launch {
            _state.update { it.copy(
                isLoading = true,
                showLogoutDialog = false
            ) }

            logoutUseCase()
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        isLogoutSuccessful = true
                    ) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cerrar sesión"
                    ) }
                }
        }
    }
}