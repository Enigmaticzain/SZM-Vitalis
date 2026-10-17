package com.example.humandigitaltwin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.humandigitaltwin.data.models.NutritionLog

@Entity(tableName = "meals")
data class MealEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mealName: String,
    val carbsG: Double,
    val proteinG: Double,
    val fatG: Double,
    val calories: Double,
    val predictedGlucoseSpike: Double,
    val loggedAt: String = System.currentTimeMillis().toString()
) {
    fun toDomain(): NutritionLog = NutritionLog(
        meal_name = mealName,
        carbs_g = carbsG,
        protein_g = proteinG,
        fat_g = fatG,
        calories = calories,
        predicted_glucose_spike = predictedGlucoseSpike
    )

    companion object {
        fun fromDomain(log: NutritionLog): MealEntity = MealEntity(
            mealName = log.meal_name,
            carbsG = log.carbs_g,
            proteinG = log.protein_g,
            fatG = log.fat_g,
            calories = log.calories,
            predictedGlucoseSpike = log.predicted_glucose_spike
        )
    }
}
