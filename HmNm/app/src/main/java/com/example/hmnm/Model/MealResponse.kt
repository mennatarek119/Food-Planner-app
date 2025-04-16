package com.example.hmnm.Model
import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize

data class MealResponse(val meals: List<Meal>)

@Parcelize
data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strMealThumb: String,
    val strArea: String?,
    val strInstructions: String?,
    val strYoutube: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,
    val ingredientCount: Int = 0,
    var prepTime: Int? = null,
    var rating: Float? = null,
    var isFavorite: Boolean = false
) : Parcelable


