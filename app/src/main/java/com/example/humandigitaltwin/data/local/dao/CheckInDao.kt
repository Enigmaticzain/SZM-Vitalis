package com.example.humandigitaltwin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.humandigitaltwin.data.local.entity.CheckInEntity

@Dao
interface CheckInDao {
    @Query("SELECT * FROM remote_checkins ORDER BY id DESC")
    suspend fun getAllCheckIns(): List<CheckInEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: CheckInEntity): Long

    @Query("DELETE FROM remote_checkins")
    suspend fun deleteAllCheckIns()
}
