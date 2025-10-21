package com.example.proyecto.data.mapper

import com.example.proyecto.data.local.entity.*
import com.example.proyecto.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

// Space Mappers
fun SpaceEntity.toDomain(): Space = Space(
    id = id,
    name = name,
    type = SpaceType.valueOf(type),
    description = description,
    capacity = capacity,
    isActive = isActive
)

fun Space.toEntity(): SpaceEntity = SpaceEntity(
    id = id,
    name = name,
    type = type.name,
    description = description,
    capacity = capacity,
    isActive = isActive
)

// TimeSlot Mappers
fun TimeSlotEntity.toDomain(): TimeSlot = TimeSlot(
    id = id,
    spaceId = spaceId,
    date = LocalDate.parse(date),
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    status = SlotStatus.valueOf(status),
    reservedBy = reservedBy,
    reservedByName = reservedByName,
    description = description
)

fun TimeSlot.toEntity(): TimeSlotEntity = TimeSlotEntity(
    id = id,
    spaceId = spaceId,
    date = date.toString(),
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    status = status.name,
    reservedBy = reservedBy,
    reservedByName = reservedByName,
    description = description
)

// Reservation Mappers
fun ReservationEntity.toDomain(): Reservation = Reservation(
    id = id,
    spaceId = spaceId,
    spaceName = spaceName,
    spaceType = SpaceType.valueOf(spaceType),
    userId = userId,
    userName = userName,
    userEmail = userEmail,
    date = LocalDate.parse(date),
    startTime = LocalTime.parse(startTime),
    endTime = LocalTime.parse(endTime),
    description = description,
    status = ReservationStatus.valueOf(status),
    createdAt = createdAt,
    approvedBy = approvedBy,
    rejectionReason = rejectionReason
)

fun Reservation.toEntity(): ReservationEntity = ReservationEntity(
    id = id,
    spaceId = spaceId,
    spaceName = spaceName,
    spaceType = spaceType.name,
    userId = userId,
    userName = userName,
    userEmail = userEmail,
    date = date.toString(),
    startTime = startTime.toString(),
    endTime = endTime.toString(),
    description = description,
    status = status.name,
    createdAt = createdAt,
    approvedBy = approvedBy,
    rejectionReason = rejectionReason
)

// User Mappers
fun UserCacheEntity.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    role = UserRole.valueOf(role)
)

fun User.toEntity(): UserCacheEntity = UserCacheEntity(
    id = id,
    name = name,
    email = email,
    role = role.name
)

// Firebase Document Mappers
fun Map<String, Any>.toSpace(): Space = Space(
    id = this["id"] as? String ?: "",
    name = this["name"] as? String ?: "",
    type = SpaceType.valueOf(this["type"] as? String ?: "CANCHA"),
    description = this["description"] as? String ?: "",
    capacity = (this["capacity"] as? Long)?.toInt() ?: 0,
    isActive = this["isActive"] as? Boolean ?: true
)

fun Map<String, Any>.toTimeSlot(): TimeSlot = TimeSlot(
    id = this["id"] as? String ?: "",
    spaceId = this["spaceId"] as? String ?: "",
    date = LocalDate.parse(this["date"] as? String ?: ""),
    startTime = LocalTime.parse(this["startTime"] as? String ?: ""),
    endTime = LocalTime.parse(this["endTime"] as? String ?: ""),
    status = SlotStatus.valueOf(this["status"] as? String ?: "AVAILABLE"),
    reservedBy = this["reservedBy"] as? String,
    reservedByName = this["reservedByName"] as? String,
    description = this["description"] as? String
)

fun Map<String, Any>.toReservation(): Reservation = Reservation(
    id = this["id"] as? String ?: "",
    spaceId = this["spaceId"] as? String ?: "",
    spaceName = this["spaceName"] as? String ?: "",
    spaceType = SpaceType.valueOf(this["spaceType"] as? String ?: "CANCHA"),
    userId = this["userId"] as? String ?: "",
    userName = this["userName"] as? String ?: "",
    userEmail = this["userEmail"] as? String ?: "",
    date = LocalDate.parse(this["date"] as? String ?: ""),
    startTime = LocalTime.parse(this["startTime"] as? String ?: ""),
    endTime = LocalTime.parse(this["endTime"] as? String ?: ""),
    description = this["description"] as? String ?: "",
    status = ReservationStatus.valueOf(this["status"] as? String ?: "PENDING"),
    createdAt = this["createdAt"] as? Long ?: 0L,
    approvedBy = this["approvedBy"] as? String,
    rejectionReason = this["rejectionReason"] as? String
)