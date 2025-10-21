package com.example.proyecto.data.local.dao

import androidx.room.*
import com.example.proyecto.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SpaceDao {
    @Query("SELECT * FROM spaces WHERE isActive = 1")
    fun getAllSpaces(): Flow<List<SpaceEntity>>

    @Query("SELECT * FROM spaces WHERE id = :spaceId")
    suspend fun getSpaceById(spaceId: String): SpaceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpaces(spaces: List<SpaceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: SpaceEntity)

    @Query("DELETE FROM spaces")
    suspend fun clearAll()
}

@Dao
interface TimeSlotDao {
    @Query("SELECT * FROM time_slots WHERE spaceId = :spaceId AND date = :date ORDER BY startTime")
    fun getTimeSlotsBySpaceAndDate(spaceId: String, date: String): Flow<List<TimeSlotEntity>>

    @Query("SELECT * FROM time_slots WHERE date >= :startDate AND date <= :endDate")
    fun getTimeSlotsInRange(startDate: String, endDate: String): Flow<List<TimeSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(slots: List<TimeSlotEntity>)

    @Query("DELETE FROM time_slots WHERE spaceId = :spaceId AND date = :date")
    suspend fun deleteSlotsBySpaceAndDate(spaceId: String, date: String)

    @Query("DELETE FROM time_slots WHERE updatedAt < :threshold")
    suspend fun deleteOldSlots(threshold: Long)
}

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserReservations(userId: String): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE id = :reservationId")
    suspend fun getReservationById(reservationId: String): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE date >= :date ORDER BY date, startTime")
    fun getUpcomingReservations(date: String): Flow<List<ReservationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservation(reservation: ReservationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReservations(reservations: List<ReservationEntity>)

    @Query("DELETE FROM reservations WHERE id = :reservationId")
    suspend fun deleteReservation(reservationId: String)

    @Query("DELETE FROM reservations")
    suspend fun clearAll()
}

@Dao
interface UserCacheDao {
    @Query("SELECT * FROM user_cache WHERE id = :userId")
    suspend fun getUserById(userId: String): UserCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserCacheEntity)

    @Query("DELETE FROM user_cache")
    suspend fun clearAll()
}