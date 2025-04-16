package com.example.hmnm.database.dao

import androidx.room.*
import com.example.hmnm.Model.Meal
import com.example.hmnm.database.entities.FavoriteMeal

@Dao
interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteMeal)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteMeal)

    @Query("SELECT * FROM favorites")
    suspend fun getFavorites(): List<FavoriteMeal>

    @Query("DELETE FROM favorites WHERE idMeal = :idMeal")
    suspend fun deleteFavoriteById(idMeal: String)

    @Query("SELECT * FROM favorites WHERE idMeal = :idMeal LIMIT 1")
    suspend fun getFavoriteById(idMeal: String): FavoriteMeal?

    @Update // دالة لتحديث الوجبة
    suspend fun updateFavorite(favoriteMeal: FavoriteMeal)

    @Query("SELECT COUNT(*) FROM favorites WHERE idMeal = :mealId")
    suspend fun isMealFavorite(mealId: String): Int

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    suspend fun getFavoritesForUser(userId: String): List<FavoriteMeal>

    @Query("SELECT * FROM favorites WHERE idMeal = :idMeal AND userId = :userId LIMIT 1")
    suspend fun getFavoriteByIdAndUser(idMeal: String, userId: String): FavoriteMeal?

}





