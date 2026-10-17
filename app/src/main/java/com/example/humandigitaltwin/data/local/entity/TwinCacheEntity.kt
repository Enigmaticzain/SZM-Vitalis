package com.example.humandigitaltwin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "twin_cache")
data class TwinCacheEntity(
    @PrimaryKey
    val key: String,
    val jsonPayload: String,
    val updatedAt: Long = System.currentTimeMillis()
)
