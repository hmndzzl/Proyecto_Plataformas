package com.example.proyecto.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto.domain.model.*
import com.example.proyecto.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

// State
data class DashboardState(
    val user: User? = null,
    val spaces: List<Space> = emptyList(),
    val currentMonth: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val calendarDays: List<CalendarDay> = emptyList(),
    val monthReservations: List<Reservation> = emptyList(), // NUEVO
    val isLoading: Boolean = false,
    val error: String? = null
)

// Events
sealed interface DashboardEvent {
    object LoadData : DashboardEvent
    data class SpaceSelected(val spaceId: String) : DashboardEvent
    data class DayClicked(val date: LocalDate) : DashboardEvent // NUEVO
    object PreviousMonth : DashboardEvent
    object NextMonth : DashboardEvent
    object ProfileClicked : DashboardEvent
    object ErrorDismissed : DashboardEvent
}

// ViewModel
class DashboardViewModel(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getSpacesUseCase: GetSpacesUseCase,
    private val getReservationsForMonthUseCase: GetReservationsForMonthUseCase // NUEVO
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        loadData()
        observeSpaces()
    }

    fun onEvent(event: DashboardEvent) {
        when (event) {
            is DashboardEvent.LoadData -> loadData()
            is DashboardEvent.SpaceSelected -> {
                // Navigation handled by UI
            }
            is DashboardEvent.DayClicked -> {
                // Navigation handled by UI
            }
            is DashboardEvent.PreviousMonth -> {
                val newMonth = _state.value.currentMonth.minus(1, DateTimeUnit.MONTH)
                updateMonth(newMonth)
            }
            is DashboardEvent.NextMonth -> {
                val newMonth = _state.value.currentMonth.plus(1, DateTimeUnit.MONTH)
                updateMonth(newMonth)
            }
            is DashboardEvent.ProfileClicked -> {
                // Navigation handled by UI
            }
            is DashboardEvent.ErrorDismissed -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Load user
                val user = getCurrentUserUseCase()

                // Sync spaces
                getSpacesUseCase.sync()

                _state.update { it.copy(
                    user = user,
                    isLoading = false
                ) }

                // Generate calendar and load reservations
                updateMonth(_state.value.currentMonth)

            } catch (e: Exception) {
                _state.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Error al cargar datos"
                ) }
            }
        }
    }

    private fun observeSpaces() {
        viewModelScope.launch {
            getSpacesUseCase().collect { spaces ->
                _state.update { it.copy(spaces = spaces) }
            }
        }
    }

    private fun updateMonth(newMonth: LocalDate) {
        viewModelScope.launch {
            try {
                val firstDay = LocalDate(newMonth.year, newMonth.month, 1)
                val lastDay = LocalDate(newMonth.year, newMonth.month, newMonth.month.length(isLeapYear(newMonth.year)))

                // Sync reservations for the month
                getReservationsForMonthUseCase.sync(firstDay, lastDay)

                // Observe reservations
                observeMonthReservations(firstDay, lastDay, newMonth)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun observeMonthReservations(startDate: LocalDate, endDate: LocalDate, month: LocalDate) {
        viewModelScope.launch {
            getReservationsForMonthUseCase(startDate, endDate).collect { reservations ->
                val calendarDays = generateCalendarDays(month, reservations)
                _state.update { it.copy(
                    currentMonth = month,
                    calendarDays = calendarDays,
                    monthReservations = reservations
                ) }
            }
        }
    }

    private fun generateCalendarDays(month: LocalDate, reservations: List<Reservation>): List<CalendarDay> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val firstDayOfMonth = LocalDate(month.year, month.month, 1)
        val lastDayOfMonth = LocalDate(month.year, month.month, month.month.length(isLeapYear(month.year)))

        val startDayOfWeek = firstDayOfMonth.dayOfWeek.ordinal // 0 = Monday
        val daysInMonth = lastDayOfMonth.dayOfMonth

        val days = mutableListOf<CalendarDay>()

        // Add previous month days
        val prevMonth = month.minus(1, DateTimeUnit.MONTH)
        val prevMonthLastDay = LocalDate(prevMonth.year, prevMonth.month, prevMonth.month.length(isLeapYear(prevMonth.year)))
        for (i in (prevMonthLastDay.dayOfMonth - startDayOfWeek + 1)..prevMonthLastDay.dayOfMonth) {
            days.add(CalendarDay(
                date = LocalDate(prevMonth.year, prevMonth.month, i),
                reservations = emptyList(),
                hasReservations = false,
                isAvailable = false,
                isToday = false,
                isSelected = false
            ))
        }

        // Add current month days with reservations
        for (day in 1..daysInMonth) {
            val date = LocalDate(month.year, month.month, day)
            val dayReservations = reservations.filter { it.date == date }

            days.add(CalendarDay(
                date = date,
                reservations = dayReservations,
                hasReservations = dayReservations.isNotEmpty(),
                isAvailable = true,
                isToday = date == today,
                isSelected = false
            ))
        }

        // Add next month days to complete the grid
        val nextMonth = month.plus(1, DateTimeUnit.MONTH)
        val remainingDays = 42 - days.size // 6 weeks * 7 days
        for (day in 1..remainingDays) {
            days.add(CalendarDay(
                date = LocalDate(nextMonth.year, nextMonth.month, day),
                reservations = emptyList(),
                hasReservations = false,
                isAvailable = false,
                isToday = false,
                isSelected = false
            ))
        }

        return days
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }
}