package com.example.proyecto.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// User Model
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT
)

enum class UserRole {
    STUDENT,
    STAFF,
    ADMIN
}

// Space Model (Cancha o Garden)
data class Space(
    val id: String = "",
    val name: String = "",
    val type: SpaceType,
    val description: String = "",
    val capacity: Int = 0,
    val isActive: Boolean = true
)

enum class SpaceType {
    CANCHA,
    GARDEN
}

// Time Slot Model
data class TimeSlot(
    val id: String = "",
    val spaceId: String = "",
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: SlotStatus,
    val reservedBy: String? = null,
    val reservedByName: String? = null,
    val description: String? = null
)

enum class SlotStatus {
    AVAILABLE,
    RESERVED,
    PENDING_APPROVAL,
    BLOCKED
}

// Reservation Model
data class Reservation(
    val id: String = "",
    val spaceId: String = "",
    val spaceName: String = "",
    val spaceType: SpaceType,
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String = "",
    val status: ReservationStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val approvedBy: String? = null,
    val rejectionReason: String? = null
)

enum class ReservationStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED
}

// Calendar Day Model
// Calendar Day Model
data class CalendarDay(
    val date: LocalDate,
    val reservations: List<Reservation> = emptyList(),
    val hasReservations: Boolean = reservations.isNotEmpty(),
    val isAvailable: Boolean = true,
    val isToday: Boolean = false,
    val isSelected: Boolean = false
)

// Create Reservation Request
data class CreateReservationRequest(
    val spaceId: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val description: String
)