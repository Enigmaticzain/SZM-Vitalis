package com.example.humandigitaltwin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.humandigitaltwin.data.local.dao.ChatMessageDao
import com.example.humandigitaltwin.data.local.dao.CheckInDao
import com.example.humandigitaltwin.data.local.dao.MealDao
import com.example.humandigitaltwin.data.local.dao.TwinCacheDao
import com.example.humandigitaltwin.data.local.entity.ChatMessageEntity
import com.example.humandigitaltwin.data.local.entity.CheckInEntity
import com.example.humandigitaltwin.data.local.entity.MealEntity
import com.example.humandigitaltwin.data.local.entity.TwinCacheEntity

@Database(
    entities = [
        MealEntity::class,
        ChatMessageEntity::class,
        CheckInEntity::class,
        TwinCacheEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TwinDatabase : RoomDatabase() {
    abstract fun mealDao(): MealDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun checkInDao(): CheckInDao
    abstract fun twinCacheDao(): TwinCacheDao

    companion object {
        @Volatile
        private var INSTANCE: TwinDatabase? = null

        fun getInstance(context: Context): TwinDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TwinDatabase::class.java,
                    "human_digital_twin.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
