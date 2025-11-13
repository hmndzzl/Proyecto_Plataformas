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
            spaceDao.clearAll()

            val snapshot = firestore.collection("spaces")
                .whereEqualTo("isActive", true)
                .get()
                .await()

            val spaces = snapshot.documents
                .mapNotNull { doc -> doc.data?.toSpace()?.toEntity() }
                .distinctBy { it.id } // Eliminar duplicados
                .filter { it.id.isNotBlank() } // Filtrar IDs vacíos

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
            println("DEBUG: Sincronizando slots para space=$spaceId, date=$date")

            // Obtener slots base de Firestore (configuración del espacio)
            val slotsSnapshot = firestore.collection("time_slots")
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("date", date.toString())
                .get()
                .await()

            println("DEBUG: Slots encontrados en Firestore: ${slotsSnapshot.documents.size}")

            // Obtener reservas aprobadas para este día
            val reservationsSnapshot = firestore.collection("reservations")
                .whereEqualTo("spaceId", spaceId)
                .whereEqualTo("date", date.toString())
                .whereIn("status", listOf("APPROVED", "PENDING"))
                .get()
                .await()

            println("DEBUG: Reservas encontradas: ${reservationsSnapshot.documents.size}")

            // Crear mapa de slots ocupados
            val reservedSlots = reservationsSnapshot.documents.mapNotNull { doc ->
                doc.data?.toReservation()
            }

            // Generar slots con estado actualizado
            val slots = if (slotsSnapshot.documents.isEmpty()) {
                // Si no hay slots configurados, generar slots por defecto (8:00 - 18:00)
                generateDefaultSlots(spaceId, date, reservedSlots)
            } else {
                // Si hay slots configurados, actualizarlos con las reservas
                slotsSnapshot.documents.mapNotNull { doc ->
                    val slotData = doc.data
                    if (slotData != null) {
                        val slot = slotData.toTimeSlot()
                        // Verificar si este slot está reservado
                        val reservation = reservedSlots.find { res ->
                            res.startTime == slot.startTime && res.endTime == slot.endTime
                        }

                        if (reservation != null) {
                            // Actualizar slot con información de reserva
                            slot.copy(
                                status = if (reservation.status == ReservationStatus.APPROVED) {
                                    SlotStatus.RESERVED
                                } else {
                                    SlotStatus.PENDING_APPROVAL
                                },
                                reservedBy = reservation.userId,
                                reservedByName = reservation.userName,
                                description = reservation.description
                            )
                        } else {
                            slot
                        }
                    } else null
                }
            }

            println("DEBUG: Slots procesados: ${slots.size}")
            slots.forEach { slot ->
                println("DEBUG: Slot ${slot.startTime}-${slot.endTime}: ${slot.status}, reservedBy=${slot.reservedByName}")
            }

            // Guardar en base de datos local
            timeSlotDao.deleteSlotsBySpaceAndDate(spaceId, date.toString())
            timeSlotDao.insertTimeSlots(slots.map { it.toEntity() })
        } catch (e: Exception) {
            println("ERROR: Sincronizando slots: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun generateDefaultSlots(
        spaceId: String,
        date: LocalDate,
        reservations: List<Reservation>
    ): List<TimeSlot> {
        val slots = mutableListOf<TimeSlot>()

        // Generar slots de 7:00 a 21:00 (cada 1 hora)
        for (hour in 7..20) {
            val startTime = kotlinx.datetime.LocalTime(hour, 0)
            val endTime = kotlinx.datetime.LocalTime(hour + 1, 0)

            val reservation = reservations.find { res ->
                (res.startTime >= startTime && res.startTime < endTime) ||
                        (res.endTime > startTime && res.endTime <= endTime) ||
                        (res.startTime <= startTime && res.endTime >= endTime)
            }

            slots.add(TimeSlot(
                id = "$spaceId-$date-$hour",
                spaceId = spaceId,
                date = date,
                startTime = startTime,
                endTime = endTime,
                status = if (reservation != null) {
                    if (reservation.status == ReservationStatus.APPROVED) {
                        SlotStatus.RESERVED
                    } else {
                        SlotStatus.PENDING_APPROVAL
                    }
                } else {
                    SlotStatus.AVAILABLE
                },
                reservedBy = reservation?.userId,
                reservedByName = reservation?.userName,
                description = reservation?.description
            ))
        }

        return slots
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

    fun getReservationsForDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Reservation>> {
        return reservationDao.getUpcomingReservations(startDate.toString())
            .map { entities ->
                entities.filter { entity ->
                    val reservationDate = LocalDate.parse(entity.date)
                    reservationDate >= startDate && reservationDate <= endDate &&
                            entity.status == ReservationStatus.APPROVED.name
                }.map { it.toDomain() }
            }
    }

    suspend fun syncReservationsForDateRange(startDate: LocalDate, endDate: LocalDate) {
        try {
            val snapshot = firestore.collection("reservations")
                .whereEqualTo("status", ReservationStatus.APPROVED.name)
                .whereGreaterThanOrEqualTo("date", startDate.toString())
                .whereLessThanOrEqualTo("date", endDate.toString())
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
}

