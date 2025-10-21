package com.example.proyecto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.proyecto.data.local.dao.*
import com.example.proyecto.data.local.entity.*

@Database(
    entities = [
        SpaceEntity::class,
        TimeSlotEntity::class,
        ReservationEntity::class,
        UserCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun spaceDao(): SpaceDao
    abstract fun timeSlotDao(): TimeSlotDao
    abstract fun reservationDao(): ReservationDao
    abstract fun userCacheDao(): UserCacheDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uvg_espacios_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}