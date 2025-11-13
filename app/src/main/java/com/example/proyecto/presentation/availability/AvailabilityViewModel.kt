package com.example.proyecto.presentation.availability

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.*
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.datetime.*

// State
data class AvailabilityState(
    val space: Space? = null,
    val currentDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val timeSlots: List<TimeSlot> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events
sealed interface AvailabilityEvent {
    data class LoadAvailability(val spaceId: String) : AvailabilityEvent
    object PreviousDay : AvailabilityEvent
    object NextDay : AvailabilityEvent
    object ReserveClicked : AvailabilityEvent
    object ErrorDismissed : AvailabilityEvent
}

// ViewModel
class AvailabilityViewModel(
    private val getSpaceByIdUseCase: GetSpaceByIdUseCase,
    private val getTimeSlotsUseCase: GetTimeSlotsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AvailabilityState())
    val state: StateFlow<AvailabilityState> = _state.asStateFlow()

    private var currentSpaceId: String? = null

    fun onEvent(event: AvailabilityEvent) {
        when (event) {
            is AvailabilityEvent.LoadAvailability -> {
                currentSpaceId = event.spaceId
                loadAvailability(event.spaceId, _state.value.currentDate)
                startAutoSync(event.spaceId)
            }

            is AvailabilityEvent.PreviousDay -> {
                val newDate = _state.value.currentDate.minus(1, DateTimeUnit.DAY)
                updateDate(newDate)
            }

            is AvailabilityEvent.NextDay -> {
                val newDate = _state.value.currentDate.plus(1, DateTimeUnit.DAY)
                updateDate(newDate)
            }

            is AvailabilityEvent.ReserveClicked -> {
                // Navigation handled by UI
            }

            is AvailabilityEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadAvailability(spaceId: String, date: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                // Load space info
                val space = getSpaceByIdUseCase(spaceId)

                if (space == null) {
                    _state.update { it.copy(
                        isLoading = false,
                        error = "Espacio no encontrado"
                    ) }
                    return@launch
                }

                // Sync time slots
                getTimeSlotsUseCase.sync(spaceId, date)

                _state.update { it.copy(
                    space = space,
                    currentDate = date,
                    isLoading = false
                ) }

                // Observe time slots
                observeTimeSlots(spaceId, date)

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar disponibilidad"
                ) }
            }
        }
    }

    private fun observeTimeSlots(spaceId: String, date: LocalDate) {
        viewModelScope.launch {
            getTimeSlotsUseCase(spaceId, date).collect { slots ->
                _state.update { it.copy(timeSlots = slots) }
            }
        }
    }

    private fun updateDate(newDate: LocalDate) {
        val spaceId = currentSpaceId ?: return
        _state.update { it.copy(currentDate = newDate) }
        loadAvailability(spaceId, newDate)
    }

    // Sincronización automática cada 10 segundos
    private fun startAutoSync(spaceId: String) {
        viewModelScope.launch {
            while (true) {
                delay(10_000) // 10 segundos
                try {
                    val currentDate = _state.value.currentDate
                    println("DEBUG: Auto-sync de slots...")
                    getTimeSlotsUseCase.sync(spaceId, currentDate)
                } catch (e: Exception) {
                    println("ERROR: Auto-sync falló: ${e.message}")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("DEBUG: AvailabilityViewModel cleared")
    }
}