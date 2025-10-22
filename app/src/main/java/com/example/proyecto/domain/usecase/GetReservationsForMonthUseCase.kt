package com.example.proyecto.domain.usecase

import com.example.proyecto.data.repository.SpaceRepository
import com.example.proyecto.domain.model.Reservation
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class GetReservationsForMonthUseCase(private val spaceRepository: SpaceRepository) {
    suspend fun sync(startDate: LocalDate, endDate: LocalDate) {
        spaceRepository.syncReservationsForDateRange(startDate, endDate)
    }

    operator fun invoke(startDate: LocalDate, endDate: LocalDate): Flow<List<Reservation>> {
        return spaceRepository.getReservationsForDateRange(startDate, endDate)
    }
}