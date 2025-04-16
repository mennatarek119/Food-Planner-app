package com.example.hmnm.Model

data class IngredientResponse(
    val meals: List<Ingredient>
)

data class Ingredient(
    val strIngredient: String
) {
    val ingredientsImage: String
        get() = "https://www.themealdb.com/images/ingredients/${strIngredient}.png"
}
