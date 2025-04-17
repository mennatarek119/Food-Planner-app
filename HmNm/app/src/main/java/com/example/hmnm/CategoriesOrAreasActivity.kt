package com.example.hmnm

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.Model.Area
import com.example.hmnm.Model.Category
import com.example.hmnm.Model.Meal
import com.example.hmnm.adapter.AreaAdapter
import com.example.hmnm.adapter.CategoryAdapter
import com.example.hmnm.adapter.MealsAdapter
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.network.ApiClient
import com.example.hmnm.utils.getFlagResId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesOrAreasActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TYPE = "extra_type"
        const val TYPE_CATEGORY = "category"
        const val TYPE_COUNTRY = "country"
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var categoriesAdapter: CategoryAdapter
    private lateinit var countriesAdapter: AreaAdapter
    private lateinit var mealsAdapter: MealsAdapter

    private var allCategories: List<Category> = emptyList()
    private var allCountries: List<Area> = emptyList()
    private var isShowingMeals = false
    private var currentType: String = TYPE_CATEGORY  

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories_areas)
        supportActionBar?.hide()

        searchView = findViewById(R.id.categorySearchView)
        searchView.clearFocus()
        setupSearch()

        recyclerView = findViewById(R.id.categoryRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        mealsAdapter = MealsAdapter(emptyList(), this)


        currentType = intent.getStringExtra(EXTRA_TYPE) ?: TYPE_CATEGORY

        if (currentType == TYPE_CATEGORY) {
            categoriesAdapter = CategoryAdapter(emptyList()) { categoryName ->
                searchView.setQuery(categoryName, true)
            }
            recyclerView.adapter = categoriesAdapter
            loadCategories()
        } else {
            countriesAdapter = AreaAdapter(emptyList()) { countryName ->
                searchView.setQuery(countryName, true)
            }
            recyclerView.adapter = countriesAdapter
            loadCountries()
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getCategories()
                }

                val categoryList = response?.categories ?: emptyList()

                withContext(Dispatchers.Main) {
                    if (categoryList.isNotEmpty()) {
                        allCategories = categoryList
                        recyclerView.adapter = categoriesAdapter
                        categoriesAdapter.updateItems(allCategories)
                    } else {
                        Toast.makeText(this@CategoriesOrAreasActivity, "No categories found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoriesOrAreasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadCountries() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getAreas()
                }

                val countryList = response?.meals ?: emptyList()

                withContext(Dispatchers.Main) {
                    if (countryList.isNotEmpty()) {
                        allCountries = countryList
                        val formattedCountries = allCountries.map { Pair(it.strArea, getFlagResId(it.strArea)) }
                        recyclerView.adapter = countriesAdapter
                        countriesAdapter.updateItems(formattedCountries)
                    } else {
                        Toast.makeText(this@CategoriesOrAreasActivity, "No countries found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CategoriesOrAreasActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupSearch() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    filterItems(query) 
                } else {
                    resetToDefaultList()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    showSuggestions(newText) 
                } else {
                    resetToDefaultList()
                }
                return true
            }
        })

        searchView.setOnCloseListener {
            resetToDefaultList()
            false
        }
    }



    private fun showSuggestions(query: String?) {
        if (!query.isNullOrEmpty()) {
            if (currentType == TYPE_CATEGORY) {
                val filteredCategories = allCategories.filter { it.strCategory.contains(query, ignoreCase = true) }
                if (filteredCategories.isNotEmpty()) {
                    recyclerView.layoutManager = GridLayoutManager(this, 2) 
                    categoriesAdapter.updateItems(filteredCategories)
                    recyclerView.adapter = categoriesAdapter
                }
            } else {
                val filteredCountries = allCountries.filter { it.strArea.contains(query, ignoreCase = true) }
                if (filteredCountries.isNotEmpty()) {
                    recyclerView.layoutManager = GridLayoutManager(this, 2) 
                    val formattedCountries = filteredCountries.map { Pair(it.strArea, getFlagResId(it.strArea)) }
                    countriesAdapter.updateItems(formattedCountries)
                    recyclerView.adapter = countriesAdapter
                }
            }
        } else {
            resetToDefaultList()
        }
    }



    private fun resetToDefaultList() {
        if (currentType == TYPE_CATEGORY) {
            recyclerView.adapter = categoriesAdapter
            categoriesAdapter.updateItems(allCategories)
        } else {
            recyclerView.adapter = countriesAdapter
            val formattedCountries = allCountries.map { Pair(it.strArea, getFlagResId(it.strArea)) }
            countriesAdapter.updateItems(formattedCountries)
        }
    }


    private fun filterItems(query: String?) {
        if (!query.isNullOrEmpty()) {
            if (currentType == TYPE_CATEGORY) {
                
                val selectedCategory = allCategories.find { it.strCategory.equals(query, ignoreCase = true) }

                if (selectedCategory != null) {
                    isShowingMeals = true
                    loadMealsByCategory(selectedCategory.strCategory)
                } else {
                    Toast.makeText(this, "No matching categories found!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val selectedCountry = allCountries.find { it.strArea.equals(query, ignoreCase = true) }

                if (selectedCountry != null) {
                    isShowingMeals = true
                    loadMealsByCountry(selectedCountry.strArea)
                } else {
                    Toast.makeText(this, "No matching countries found!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            isShowingMeals = false
            if (currentType == TYPE_CATEGORY) {
                recyclerView.adapter = categoriesAdapter
                categoriesAdapter.updateItems(allCategories)
            } else {
                recyclerView.adapter = countriesAdapter
                val formattedCountries = allCountries.map { Pair(it.strArea, getFlagResId(it.strArea)) }
                countriesAdapter.updateItems(formattedCountries)
            }
        }
    }




    private fun loadMealsByCategory(category: String) {
        lifecycleScope.launch {
            try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.retrofitService.getMealsByCategory(category)
            }
            val mealsList = response.meals ?: emptyList() 

            if (mealsList.isNotEmpty()) {
                val mealsWithIngredients = mealsList.map { meal ->
                    val mealDetails = withContext(Dispatchers.IO) {
                        ApiClient.retrofitService.getMealDetails(meal.idMeal).meals.firstOrNull()
                    }
                    meal.copy(ingredientCount = mealDetails?.let { getIngredientCount(it) } ?: 0)
                }

                recyclerView.adapter = mealsAdapter
                mealsAdapter.updateItems(mealsWithIngredients) 
            } else {
                Toast.makeText(this@CategoriesOrAreasActivity, "No meals found!", Toast.LENGTH_SHORT).show()
            }} catch (e: Exception) {

            }

        }
    }

    private fun loadMealsByCountry(country: String) {
        lifecycleScope.launch {
            try {
            val response = withContext(Dispatchers.IO) {
                ApiClient.retrofitService.getMealsByArea(country)
            }
            val mealsList = response.meals ?: emptyList()
            if (mealsList.isNotEmpty()) {
                val mealsWithIngredients = mealsList.map { meal ->
                    val mealDetails = withContext(Dispatchers.IO) {
                        ApiClient.retrofitService.getMealDetails(meal.idMeal).meals.firstOrNull()
                    }
                    meal.copy(ingredientCount = mealDetails?.let { getIngredientCount(it) } ?: 0)
                }
                recyclerView.adapter = mealsAdapter
                mealsAdapter.updateItems(mealsWithIngredients)
            } else {
                Toast.makeText(this@CategoriesOrAreasActivity, "No meals found!", Toast.LENGTH_SHORT).show()
            }} catch (e: Exception) {

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
