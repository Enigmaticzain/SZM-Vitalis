package com.example.humandigitaltwin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.humandigitaltwin.data.local.entity.MealEntity

@Dao
interface MealDao {
    @Query("SELECT * FROM meals ORDER BY id DESC")
    suspend fun getAllMeals(): List<MealEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealEntity): Long

    @Update
    suspend fun updateMeal(meal: MealEntity)

    @Query("DELETE FROM meals WHERE id = :id")
    suspend fun deleteMealById(id: Long)

    @Query("DELETE FROM meals WHERE mealName = :name")
    suspend fun deleteMealByName(name: String)

    @Query("DELETE FROM meals")
    suspend fun deleteAllMeals()

    @Query("SELECT COUNT(*) FROM meals")
    suspend fun getMealCount(): Int
}
