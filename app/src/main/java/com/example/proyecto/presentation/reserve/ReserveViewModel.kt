package com.example.proyecto.presentation.reserve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.*
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

// State
data class ReserveState(
    val space: Space? = null,
    val user: User? = null,
    val date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val description: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isReserveSuccessful: Boolean = false
)

// Events
sealed interface ReserveEvent {
    data class Initialize(val spaceId: String) : ReserveEvent
    data class DateChanged(val date: LocalDate) : ReserveEvent
    data class StartTimeChanged(val time: LocalTime) : ReserveEvent
    data class EndTimeChanged(val time: LocalTime) : ReserveEvent
    data class DescriptionChanged(val description: String) : ReserveEvent
    object ConfirmReservation : ReserveEvent
    object ErrorDismissed : ReserveEvent
    object SuccessDismissed : ReserveEvent
}

// ViewModel
class ReserveViewModel(
    private val getSpaceByIdUseCase: GetSpaceByIdUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val createReservationUseCase: CreateReservationUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ReserveState())
    val state: StateFlow<ReserveState> = _state.asStateFlow()

    fun onEvent(event: ReserveEvent) {
        when (event) {
            is ReserveEvent.Initialize -> initialize(event.spaceId)

            is ReserveEvent.DateChanged -> {
                _state.update { it.copy(date = event.date, error = null) }
            }

            is ReserveEvent.StartTimeChanged -> {
                _state.update { it.copy(startTime = event.time, error = null) }
            }

            is ReserveEvent.EndTimeChanged -> {
                _state.update { it.copy(endTime = event.time, error = null) }
            }

            is ReserveEvent.DescriptionChanged -> {
                _state.update { it.copy(description = event.description, error = null) }
            }

            is ReserveEvent.ConfirmReservation -> confirmReservation()

            is ReserveEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }

            is ReserveEvent.SuccessDismissed -> {
                _state.update { it.copy(isReserveSuccessful = false) }
            }
        }
    }

    private fun initialize(spaceId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                println("DEBUG Reserve: Inicializando con spaceId=$spaceId")
                val space = getSpaceByIdUseCase(spaceId)
                val user = getCurrentUserUseCase()

                println("DEBUG Reserve: Space=${space?.name}, User=${user?.name}")

                if (space == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Espacio no encontrado"
                    ) }
                    return@launch
                }

                if (user == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    ) }
                    return@launch
                }

                _state.update { it.copy(
                    space = space,
                    user = user,
                    isLoading = false
                ) }

            } catch (e: Exception) {
                println("ERROR Reserve Initialize: ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos"
                ) }
            }
        }
    }

    private fun confirmReservation() {
        val currentState = _state.value

        println("DEBUG Reserve: Confirmando reserva...")
        println("DEBUG Reserve: startTime=${currentState.startTime}, endTime=${currentState.endTime}")
        println("DEBUG Reserve: description='${currentState.description}'")

        // Validation
        if (currentState.startTime == null) {
            println("DEBUG Reserve: Error - No start time")
            _state.update { it.copy(error = "Seleccione hora de inicio") }
            return
        }

        if (currentState.endTime == null) {
            println("DEBUG Reserve: Error - No end time")
            _state.update { it.copy(error = "Seleccione hora de fin") }
            return
        }

        if (currentState.description.isBlank()) {
            println("DEBUG Reserve: Error - No description")
            _state.update { it.copy(error = "Ingrese una descripción") }
            return
        }

        val space = currentState.space
        val user = currentState.user

        if (space == null || user == null) {
            println("DEBUG Reserve: Error - Missing space or user")
            _state.update { it.copy(error = "Datos incompletos") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                println("DEBUG Reserve: Creando request...")

                val request = CreateReservationRequest(
                    spaceId = space.id,
                    date = currentState.date,
                    startTime = currentState.startTime!!,
                    endTime = currentState.endTime!!,
                    description = currentState.description
                )

                println("DEBUG Reserve: Llamando a createReservationUseCase...")

                createReservationUseCase(request, user)
                    .onSuccess { reservationId ->
                        println("DEBUG Reserve: ✅ Reserva creada exitosamente: $reservationId")
                        _state.update { it.copy(
                            isLoading = false,
                            isReserveSuccessful = true
                        ) }
                    }
                    .onFailure { exception ->
                        println("ERROR Reserve: ❌ Error al crear reserva: ${exception.message}")
                        exception.printStackTrace()
                        _state.update { it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al crear reserva"
                        ) }
                    }
            } catch (e: Exception) {
                println("ERROR Reserve: ❌ Excepción inesperada: ${e.message}")
                e.printStackTrace()
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error inesperado"
                ) }
            }
        }
    }
}