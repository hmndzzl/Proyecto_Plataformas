package com.example.proyecto.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.*
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// State
data class AdminState(
    val user: User? = null,
    val pendingReservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedReservation: Reservation? = null,
    val showApproveDialog: Boolean = false,
    val showRejectDialog: Boolean = false,
    val rejectionReason: String = ""
)

// Events
sealed interface AdminEvent {
    object LoadReservations : AdminEvent
    data class ReservationClicked(val reservation: Reservation) : AdminEvent
    object ApproveClicked : AdminEvent
    object ApproveConfirmed : AdminEvent
    object RejectClicked : AdminEvent
    data class RejectionReasonChanged(val reason: String) : AdminEvent
    object RejectConfirmed : AdminEvent
    object DialogDismissed : AdminEvent
    object ErrorDismissed : AdminEvent
}

// ViewModel
class AdminViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getPendingReservationsUseCase: GetPendingReservationsUseCase,
    private val approveReservationUseCase: ApproveReservationUseCase,
    private val rejectReservationUseCase: RejectReservationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadReservations()
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.LoadReservations -> loadReservations()

            is AdminEvent.ReservationClicked -> {
                _state.update { it.copy(selectedReservation = event.reservation) }
            }

            is AdminEvent.ApproveClicked -> {
                _state.update { it.copy(showApproveDialog = true) }
            }

            is AdminEvent.ApproveConfirmed -> approveReservation()

            is AdminEvent.RejectClicked -> {
                _state.update { it.copy(showRejectDialog = true) }
            }

            is AdminEvent.RejectionReasonChanged -> {
                _state.update { it.copy(rejectionReason = event.reason) }
            }

            is AdminEvent.RejectConfirmed -> rejectReservation()

            is AdminEvent.DialogDismissed -> {
                _state.update { it.copy(
                    showApproveDialog = false,
                    showRejectDialog = false,
                    rejectionReason = ""
                ) }
            }

            is AdminEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadReservations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                val user = getCurrentUserUseCase()

                if (user?.role != UserRole.ADMIN && user?.role != UserRole.STAFF) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "No tienes permisos de administrador"
                    ) }
                    return@launch
                }

                // Sync pending reservations
                getPendingReservationsUseCase.sync()

                _state.update { it.copy(
                    user = user,
                    isLoading = false
                ) }

                // Observe pending reservations
                observePendingReservations()

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar reservas"
                ) }
            }
        }
    }

    private fun observePendingReservations() {
        viewModelScope.launch {
            getPendingReservationsUseCase().collect { reservations ->
                _state.update { it.copy(pendingReservations = reservations) }
            }
        }
    }

    private fun approveReservation() {
        val reservation = _state.value.selectedReservation ?: return
        val user = _state.value.user ?: return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showApproveDialog = false) }

            approveReservationUseCase(reservation.id, user.id)
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        selectedReservation = null
                    ) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al aprobar reserva"
                    ) }
                }
        }
    }

    private fun rejectReservation() {
        val reservation = _state.value.selectedReservation ?: return
        val reason = _state.value.rejectionReason

        if (reason.isBlank()) {
            _state.update { it.copy(error = "Ingrese un motivo de rechazo") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showRejectDialog = false) }

            rejectReservationUseCase(reservation.id, reason)
                .onSuccess {
                    _state.update { it.copy(
                        isLoading = false,
                        selectedReservation = null,
                        rejectionReason = ""
                    ) }
                }
                .onFailure { exception ->
                    _state.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al rechazar reserva"
                    ) }
                }
        }
    }
}