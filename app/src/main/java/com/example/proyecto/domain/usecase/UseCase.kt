package com.example.proyecto.domain.usecase

import com.example.proyecto.data.repository.AuthRepository
import com.example.proyecto.data.repository.SpaceRepository
import com.example.proyecto.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

// Authentication Use Cases
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return authRepository.login(email, password)
    }
}

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String,
        name: String
    ): Result<User> {
        return authRepository.register(email, password, name)
    }
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return authRepository.logout()
    }
}

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? {
        return authRepository.getCurrentUser()
    }
}

class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<User?> {
        return authRepository.observeAuthState()
    }
}

// Space Use Cases
class GetSpacesUseCase(private val spaceRepository: SpaceRepository) {
    suspend fun sync() {
        spaceRepository.syncSpaces()
    }

    operator fun invoke(): Flow<List<Space>> {
        return spaceRepository.getSpaces()
    }
}

class GetSpaceByIdUseCase(private val spaceRepository: SpaceRepository) {
    suspend operator fun invoke(spaceId: String): Space? {
        return spaceRepository.getSpaceById(spaceId)
    }
}

// Time Slot Use Cases
class GetTimeSlotsUseCase(private val spaceRepository: SpaceRepository) {
    suspend fun sync(spaceId: String, date: LocalDate) {
        spaceRepository.syncTimeSlots(spaceId, date)
    }

    operator fun invoke(spaceId: String, date: LocalDate): Flow<List<TimeSlot>> {
        return spaceRepository.getTimeSlots(spaceId, date)
    }
}

// Reservation Use Cases
class CreateReservationUseCase(private val spaceRepository: SpaceRepository) {
    suspend operator fun invoke(
        request: CreateReservationRequest,
        user: User
    ): Result<String> {
        // Validation
        if (request.startTime >= request.endTime) {
            return Result.failure(Exception("La hora de inicio debe ser menor a la hora de fin"))
        }

        if (request.description.isBlank()) {
            return Result.failure(Exception("Debe proporcionar una descripción"))
        }

        return spaceRepository.createReservation(request, user)
    }
}

class GetUserReservationsUseCase(private val spaceRepository: SpaceRepository) {
    suspend fun sync(userId: String) {
        spaceRepository.syncUserReservations(userId)
    }

    operator fun invoke(userId: String): Flow<List<Reservation>> {
        return spaceRepository.getUserReservations(userId)
    }
}

class CancelReservationUseCase(private val spaceRepository: SpaceRepository) {
    suspend operator fun invoke(reservationId: String): Result<Unit> {
        return spaceRepository.cancelReservation(reservationId)
    }
}

// Validation Use Cases
class ValidateEmailUseCase {
    operator fun invoke(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@uvg\\.edu\\.gt$"))
    }
}

class ValidatePasswordUseCase {
    operator fun invoke(password: String): Boolean {
        return password.length >= 6
    }
}

class ValidateTimeSlotUseCase {
    operator fun invoke(startTime: String, endTime: String): Boolean {
        // Basic time validation
        val timeRegex = Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")
        return timeRegex.matches(startTime) && timeRegex.matches(endTime)
    }
}