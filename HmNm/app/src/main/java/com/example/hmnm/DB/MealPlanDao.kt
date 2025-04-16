package com.example.hmnm.database.dao

import androidx.room.*
import com.example.hmnm.database.entities.MealPlan

@Dao
interface MealPlanDao {

    @Query("SELECT * FROM meal_plan")
    suspend fun getAllMeals(): List<MealPlan>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: MealPlan)

    @Delete
    suspend fun deleteMeal(meal: MealPlan)

    @Update
    suspend fun updateMeal(meal: MealPlan)

    @Query("SELECT * FROM meal_plan WHERE day = :day")
    suspend fun getMealsForDay(day: String): List<MealPlan>

    @Query("SELECT * FROM meal_plan WHERE userId = :userId")
    suspend fun getAllMealsForUser(userId: String): List<MealPlan>

    @Query("SELECT * FROM meal_plan WHERE mealId = :mealId LIMIT 1")
    suspend fun getMealPlan(mealId: String): MealPlan?
}
