package com.example.proyecto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spaces")
data class SpaceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val type: String, // "CANCHA" or "GARDEN"
    val description: String,
    val capacity: Int,
    val isActive: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "time_slots")
data class TimeSlotEntity(
    @PrimaryKey
    val id: String,
    val spaceId: String,
    val date: String, // ISO format
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val status: String, // SlotStatus
    val reservedBy: String?,
    val reservedByName: String?,
    val description: String?,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reservations")
data class ReservationEntity(
    @PrimaryKey
    val id: String,
    val spaceId: String,
    val spaceName: String,
    val spaceType: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val date: String, // ISO format
    val startTime: String, // HH:mm
    val endTime: String, // HH:mm
    val description: String,
    val status: String, // ReservationStatus
    val createdAt: Long,
    val approvedBy: String?,
    val rejectionReason: String?,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_cache")
data class UserCacheEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val updatedAt: Long = System.currentTimeMillis()
)