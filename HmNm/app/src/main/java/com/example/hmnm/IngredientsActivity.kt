package com.example.hmnm

import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.adapter.IngredientsAdapter
import com.example.hmnm.adapter.MealsAdapter
import com.example.hmnm.Model.Meal
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IngredientsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private lateinit var mealsAdapter: MealsAdapter
    private lateinit var searchView: SearchView

    private var allIngredients: List<Pair<String, String>> = emptyList()
    private var isShowingMeals = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ingredients)
        supportActionBar?.hide()

        searchView = findViewById(R.id.ingredientSearchView)
        searchView.queryHint = "Search..."
        searchView.clearFocus()
        setupSearch()

        recyclerView = findViewById(R.id.ingredientsRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 3)

        ingredientsAdapter = IngredientsAdapter(emptyList()) { ingredientName ->
            searchView.setQuery(ingredientName, true)
        }

        mealsAdapter = MealsAdapter(emptyList(), this)


        recyclerView.adapter = ingredientsAdapter
        loadIngredients()
    }
    private fun loadIngredients() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getIngredients()
                }

                val ingredientsList = response.meals ?: emptyList()

                if (ingredientsList.isNotEmpty()) {
                    allIngredients = ingredientsList.map { ingredient ->
                        ingredient.strIngredient to ingredient.ingredientsImage
                    }

                    recyclerView.layoutManager = GridLayoutManager(this@IngredientsActivity, 3)
                    recyclerView.adapter = ingredientsAdapter
                    ingredientsAdapter.updateItems(allIngredients)
                } else {
                    Toast.makeText(this@IngredientsActivity, "Failed to load ingredients.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@IngredientsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    filterIngredients(query) 
                } else {
                    resetToIngredientsList() 
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                showSuggestions(newText) 
                return false
            }
        })

        searchView.setOnCloseListener {
            resetToIngredientsList() 
            false
        }

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) searchView.clearFocus()
        }
    }

    private fun showSuggestions(query: String?) {
        if (!query.isNullOrEmpty()) {
            val filteredSuggestions = allIngredients.filter { it.first.contains(query, ignoreCase = true) }

            if (filteredSuggestions.isNotEmpty()) {
                recyclerView.layoutManager = GridLayoutManager(this, 3) 
                recyclerView.adapter = ingredientsAdapter
                ingredientsAdapter.updateItems(filteredSuggestions)
            }
        } else {
            resetToIngredientsList() 
        }
    }



    private fun resetToIngredientsList() {
        isShowingMeals = false
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = ingredientsAdapter
        ingredientsAdapter.updateItems(allIngredients)
    }


    private fun filterIngredients(query: String?) {
        if (!query.isNullOrEmpty()) {
            val exactMatch = allIngredients.find { it.first.equals(query, ignoreCase = true) }

            if (exactMatch != null) {
                Log.d("Search", "Using exact ingredient for search: ${exactMatch.first}")
                isShowingMeals = true
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                recyclerView.adapter = mealsAdapter
                loadMealsByIngredient(exactMatch.first)
            } else {
                Log.d("Search", "No exact match found!")
                Toast.makeText(this, "No matching ingredients found!", Toast.LENGTH_SHORT).show()
            }
        } else {
            resetToIngredientsList()
        }
    }



    private fun loadMealsByIngredient(ingredient: String) {
        lifecycleScope.launch {
            try {
                Log.d("API", "Fetching meals for: $ingredient") 

                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getMealsByIngredient(ingredient)
                }

                val mealsList = response.meals ?: emptyList()

                if (mealsList.isNotEmpty()) {
                    Log.d("API", "Meals found: ${mealsList.size}")

                    val mealsWithIngredients = mealsList.map { meal ->
                        val mealDetails = withContext(Dispatchers.IO) {
                            ApiClient.retrofitService.getMealDetails(meal.idMeal).meals.firstOrNull()
                        }
                        meal.copy(ingredientCount = mealDetails?.let { getIngredientCount(it) } ?: 0)
                    }

                    recyclerView.adapter = mealsAdapter
                    mealsAdapter.updateItems(mealsWithIngredients)
                } else {
                    Log.d("API", "No meals found!")
                    Toast.makeText(this@IngredientsActivity, "No meals found!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("API", "Error fetching meals: ${e.message}")
            }
        }
    }





    private fun getIngredientCount(meal: Meal): Int {
        val ingredients = listOf(
            meal.strIngredient1, meal.strIngredient2, meal.strIngredient3, meal.strIngredient4,
            meal.strIngredient5, meal.strIngredient6, meal.strIngredient7, meal.strIngredient8,
            meal.strIngredient9, meal.strIngredient10, meal.strIngredient11, meal.strIngredient12,
            meal.strIngredient13, meal.strIngredient14, meal.strIngredient15, meal.strIngredient16,
            meal.strIngredient17, meal.strIngredient18, meal.strIngredient19, meal.strIngredient20
        )
        return ingredients.count { !it.isNullOrEmpty() }
    }

}
