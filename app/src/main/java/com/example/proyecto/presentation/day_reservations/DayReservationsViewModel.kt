package com.example.proyecto.presentation.day_reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.Reservation
import com.example.proyecto.domain.usecase.GetReservationsForMonthUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

// State
data class DayReservationsState(
    val date: LocalDate,
    val reservations: List<Reservation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events
sealed interface DayReservationsEvent {
    data class LoadReservations(val date: LocalDate) : DayReservationsEvent
    object ErrorDismissed : DayReservationsEvent
}

// ViewModel
class DayReservationsViewModel(
    private val getReservationsForMonthUseCase: GetReservationsForMonthUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(DayReservationsState(
        date = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault()).date
    ))
    val state: StateFlow<DayReservationsState> = _state.asStateFlow()

    fun onEvent(event: DayReservationsEvent) {
        when (event) {
            is DayReservationsEvent.LoadReservations -> {
                loadReservations(event.date)
            }
            is DayReservationsEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadReservations(date: LocalDate) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, date = date) }

            try {
                // Sync reservations for the specific date
                getReservationsForMonthUseCase.sync(date, date)

                // Observe reservations for that day
                observeDayReservations(date)

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar reservas"
                ) }
            }
        }
    }

    private fun observeDayReservations(date: LocalDate) {
        viewModelScope.launch {
            getReservationsForMonthUseCase(date, date).collect { allReservations ->
                println("DEBUG DayReservations: Recibidas ${allReservations.size} reservas totales")

                // Filter reservations for the specific date
                val dayReservations = allReservations.filter { it.date == date }
                    .sortedWith(compareBy({ it.spaceId }, { it.startTime }))

                println("DEBUG DayReservations: Filtradas ${dayReservations.size} reservas para la fecha $date")
                dayReservations.forEach { res ->
                    println("  - ${res.spaceName} | ${res.userName} | ${res.startTime}-${res.endTime}")
                }

                _state.update { it.copy(
                    reservations = dayReservations,
                    isLoading = false
                ) }
            }
        }
    }
}