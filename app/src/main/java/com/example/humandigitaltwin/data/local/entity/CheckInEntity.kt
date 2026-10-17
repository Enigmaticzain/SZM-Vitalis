package com.example.humandigitaltwin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.humandigitaltwin.data.models.CheckInEntry

@Entity(tableName = "remote_checkins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: String,
    val symptomScore: Int,
    val painLevel: Int,
    val temperatureF: Double,
    val notes: String
) {
    fun toDomain(): CheckInEntry = CheckInEntry(
        timestamp = timestamp,
        symptom_score = symptomScore,
        pain_level = painLevel,
        temperature_f = temperatureF,
        notes = notes
    )

    companion object {
        fun fromDomain(entry: CheckInEntry): CheckInEntity = CheckInEntity(
            timestamp = entry.timestamp,
            symptomScore = entry.symptom_score,
            painLevel = entry.pain_level,
            temperatureF = entry.temperature_f,
            notes = entry.notes
        )
    }
}
