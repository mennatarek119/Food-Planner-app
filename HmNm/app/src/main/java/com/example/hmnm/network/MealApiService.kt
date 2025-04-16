package com.example.hmnm.network
import com.example.hmnm.Model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    @GET("list.php?a=list")
    suspend fun getAreas(): AreaResponse

    @GET("categories.php")
    suspend fun getCategories(): CategoryResponse

    @GET("list.php?i=list")
    suspend fun getIngredients(): IngredientResponse

    @GET("random.php")
    suspend fun getRandomMeal(): MealResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealResponse

    @GET("filter.php")
    suspend fun getMealsByArea(@Query("a") area: String): MealResponse

    @GET("filter.php")
    suspend fun getMealsByIngredient(@Query("i") ingredient: String): MealResponse

    @GET("search.php")
    suspend fun searchMealByName(@Query("s") mealName: String): MealResponse

    @GET("lookup.php")
    suspend fun getMealDetails(@Query("i") mealId: String): MealResponse


}


