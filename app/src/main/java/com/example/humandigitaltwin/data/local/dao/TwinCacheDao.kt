package com.example.humandigitaltwin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.humandigitaltwin.data.local.entity.TwinCacheEntity

@Dao
interface TwinCacheDao {
    @Query("SELECT * FROM twin_cache WHERE `key` = :key LIMIT 1")
    suspend fun getCache(key: String): TwinCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putCache(entity: TwinCacheEntity)

    @Query("DELETE FROM twin_cache WHERE `key` = :key")
    suspend fun deleteCache(key: String)

    @Query("DELETE FROM twin_cache")
    suspend fun clearAllCache()
}
