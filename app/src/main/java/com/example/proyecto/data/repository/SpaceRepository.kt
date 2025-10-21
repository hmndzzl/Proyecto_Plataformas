package com.example.proyecto.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.proyecto.data.local.AppDatabase
import com.example.proyecto.data.mapper.*
import com.example.proyecto.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

import java.util.UUID

class SpaceRepository(
    private val database: AppDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val spaceDao = database.spaceDao()
    private val timeSlotDao = database.timeSlotDao()
    private val reservationDao = database.reservationDao()

    // Spaces
    suspend fun syncSpaces() {
        try {
            val snapshot = firestore.collection("spaces")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val spaces = snapshot.documents.mapNotNull { doc ->
                doc.data?.toSpace()?.toEntity()
            }

            spaceDao.insertSpaces(spaces)
        } catch (e: Exception) {
            // Si falla, usa caché
            e.printStackTrace()
        }
    }

    fun getSpaces(): Flow<List<Space>> {
        return spaceDao.getAllSpaces().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getSpaceById(spaceId: String): Space? {
        // Try cache first
        val cached = spaceDao.getSpaceById(spaceId)
        if (cached != null) return cached.toDomain()

        // Fetch from Firestore
        return try {
            val doc = firestore.collection("spaces")
                .document(spaceId)
                .get()
                .await()

            doc.data?.toSpace()?.also { space ->
                spaceDao.insertSpace(space.toEntity())
            }
        } catch (e: Exception) {
            null
        }
    }

    // Time Slots
    suspend fun syncTimeSlots(spaceId: String, date: LocalDate) {
        try {
            val snapshot = firestore.collection("time_slots")
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("date", date.toString())
                .get()
                .await()

            val slots = snapshot.documents.mapNotNull { doc ->
                doc.data?.toTimeSlot()?.toEntity()
            }

            // Clear old slots for this date and insert new ones
            timeSlotDao.deleteSlotsBySpaceAndDate(spaceId, date.toString())
            timeSlotDao.insertTimeSlots(slots)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getTimeSlots(spaceId: String, date: LocalDate): Flow<List<TimeSlot>> {
        return timeSlotDao.getTimeSlotsBySpaceAndDate(spaceId, date.toString())
            .map { entities -> entities.map { it.toDomain() } }
    }

    // Reservations
    suspend fun createReservation(
        request: CreateReservationRequest,
        user: User
    ): Result<String> {
        return try {
            val space = getSpaceById(request.spaceId) ?: return Result.failure(
                Exception("Space not found")
            )

            val reservationId = UUID.randomUUID().toString()

            val reservation = Reservation(
                id = reservationId,
                spaceId = request.spaceId,
                spaceName = space.name,
                spaceType = space.type,
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                date = request.date,
                startTime = request.startTime,
                endTime = request.endTime,
                description = request.description,
                status = ReservationStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            // Save to Firestore
            firestore.collection("reservations")
                .document(reservationId)
                .set(mapOf(
                    "id" to reservation.id,
                    "spaceId" to reservation.spaceId,
                    "spaceName" to reservation.spaceName,
                    "spaceType" to reservation.spaceType.name,
                    "userId" to reservation.userId,
                    "userName" to reservation.userName,
                    "userEmail" to reservation.userEmail,
                    "date" to reservation.date.toString(),
                    "startTime" to reservation.startTime.toString(),
                    "endTime" to reservation.endTime.toString(),
                    "description" to reservation.description,
                    "status" to reservation.status.name,
                    "createdAt" to reservation.createdAt
                ))
                .await()

            // Save to local cache
            reservationDao.insertReservation(reservation.toEntity())

            Result.success(reservationId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserReservations(userId: String): Flow<List<Reservation>> {
        return reservationDao.getUserReservations(userId)
            .map { entities -> entities.map { it.toDomain() } }
    }

    suspend fun syncUserReservations(userId: String) {
        try {
            val snapshot = firestore.collection("reservations")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val reservations = snapshot.documents.mapNotNull { doc ->
                doc.data?.toReservation()?.toEntity()
            }

            reservations.forEach { reservation ->
                reservationDao.insertReservation(reservation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun cancelReservation(reservationId: String): Result<Unit> {
        return try {
            firestore.collection("reservations")
                .document(reservationId)
                .update("status", ReservationStatus.CANCELLED.name)
                .await()

            // Update local cache
            val reservation = reservationDao.getReservationById(reservationId)
            if (reservation != null) {
                reservationDao.insertReservation(
                    reservation.copy(status = ReservationStatus.CANCELLED.name)
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getPendingReservations(): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        ).map { entities ->
            entities.filter { it.status == ReservationStatus.PENDING.name }
                .map { it.toDomain() }
        }
    }

    suspend fun syncPendingReservations() {
        try {
            val snapshot = firestore.collection("reservations")
                .whereEqualTo("status", ReservationStatus.PENDING.name)
                .get()
                .await()

            val reservations = snapshot.documents.mapNotNull { doc ->
                doc.data?.toReservation()?.toEntity()
            }

            reservations.forEach { reservation ->
                reservationDao.insertReservation(reservation)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun approveReservation(reservationId: String, approvedBy: String): Result<Unit> {
        return try {
            firestore.collection("reservations")
                .document(reservationId)
                .update(mapOf(
                    "status" to ReservationStatus.APPROVED.name,
                    "approvedBy" to approvedBy
                ))
                .await()

            val reservation = reservationDao.getReservationById(reservationId)
            if (reservation != null) {
                reservationDao.insertReservation(
                    reservation.copy(
                        status = ReservationStatus.APPROVED.name,
                        approvedBy = approvedBy
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectReservation(reservationId: String, reason: String): Result<Unit> {
        return try {
            firestore.collection("reservations")
                .document(reservationId)
                .update(mapOf(
                    "status" to ReservationStatus.REJECTED.name,
                    "rejectionReason" to reason
                ))
                .await()

            val reservation = reservationDao.getReservationById(reservationId)
            if (reservation != null) {
                reservationDao.insertReservation(
                    reservation.copy(
                        status = ReservationStatus.REJECTED.name,
                        rejectionReason = reason
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}