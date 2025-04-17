package com.example.hmnm.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hmnm.Model.Meal

@Entity(tableName = "favorites")
data class FavoriteMeal(
    @PrimaryKey val idMeal: String="",
    val userId: String="",
    val strMeal: String="",
    val strMealThumb: String="",
    val strArea: String? = null,
    val strInstructions: String? = null,
    val strYoutube: String? = null,
    val strIngredient1: String? = null,
    val strIngredient2: String? = null,
    val strIngredient3: String? = null,
    val strIngredient4: String? = null,
    val strIngredient5: String? = null,
    val strIngredient6: String? = null,
    val strIngredient7: String? = null,
    val strIngredient8: String? = null,
    val strIngredient9: String? = null,
    val strIngredient10: String? = null,
    val strIngredient11: String? = null,
    val strIngredient12: String? = null,
    val strIngredient13: String? = null,
    val strIngredient14: String? = null,
    val strIngredient15: String? = null,
    val strIngredient16: String? = null,
    val strIngredient17: String? = null,
    val strIngredient18: String? = null,
    val strIngredient19: String? = null,
    val strIngredient20: String? = null,
    var ingredientCount: Int = 0,
    var prepTime: Int? = null,
    var rating: Float? = null,
    var isFavorite: Boolean = false
)

@Entity(tableName = "meal_plan")
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val day: String="day", 
    val mealId: String="mealid", 
    val userId: String="userId",
    val mealName: String="mealName",
    val mealThumb: String="",
    var isFavorite: Boolean = false
)


