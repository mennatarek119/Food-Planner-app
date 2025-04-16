package com.example.hmnm

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.Model.Meal
import com.example.hmnm.adapter.MealsAdapter
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealsActivity : AppCompatActivity() {
    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var mealsAdapter: MealsAdapter
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var categoryOrIngredientName: String
    private var isCategory: Boolean = true
    private var isIngredient: Boolean = false
    private var isArea: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.items_recipe)

        // *تهيئة العناصر*
        mealsRecyclerView = findViewById(R.id.ReciperecyclerView)
        loadingProgressBar = findViewById(R.id.progressBar)

        // *تحديد تخطيط RecyclerView*
        mealsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        mealsAdapter = MealsAdapter(emptyList(), this)

        mealsRecyclerView.adapter = mealsAdapter

        // *استقبال البيانات من الـ Intent*
        categoryOrIngredientName = intent.getStringExtra("NAME") ?: return
        isCategory = intent.getBooleanExtra("IS_CATEGORY", true)
        isIngredient = intent.getBooleanExtra("IS_INGREDIENT", false)
        isArea = intent.getBooleanExtra("IS_AREA", true) // ✅ استقبال متغير الدولة



        title = categoryOrIngredientName  // *تحديد عنوان الصفحة*


        // *تحميل الوجبات بناءً على المصدر*
        loadMeals()
    }

    private fun loadMeals() {
        lifecycleScope.launch {
            loadingProgressBar.visibility = View.VISIBLE
            mealsRecyclerView.visibility = View.GONE

            try {
                val response = withContext(Dispatchers.IO) {
                    when {
                        isCategory -> ApiClient.retrofitService.getMealsByCategory(categoryOrIngredientName)
                        isIngredient -> ApiClient.retrofitService.getMealsByIngredient(categoryOrIngredientName)
                        isArea -> ApiClient.retrofitService.getMealsByArea(categoryOrIngredientName) // ✅ جلب الوجبات حسب الدولة
                        else -> null
                    }
                }

                response?.meals?.let { mealList ->
                    val db = AppDatabase.getDatabase(this@MealsActivity)
                    val favoriteDao = db.favoriteDao()

                    val mealsWithFavorites = withContext(Dispatchers.IO) {
                        mealList.map { meal ->
                            val detailsResponse = ApiClient.retrofitService.getMealDetails(meal.idMeal)
                            val mealDetails = detailsResponse.meals.firstOrNull()
                            val ingredientCount = mealDetails?.let { getIngredientCount(it) } ?: 0
                            val isFavorite = favoriteDao.getFavoriteById(meal.idMeal) != null

                            meal.copy(ingredientCount = ingredientCount, isFavorite = isFavorite)
                        }
                    }

                    mealsAdapter.updateItems(mealsWithFavorites)
                    mealsRecyclerView.visibility = View.VISIBLE
                } ?: showErrorMessage("لم يتم العثور على وجبات تحتوي على $categoryOrIngredientName.")

            } catch (e: Exception) {
                showErrorMessage("حدث خطأ أثناء تحميل البيانات.")
            } finally {
                loadingProgressBar.visibility = View.GONE
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

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        mealsRecyclerView.visibility = View.GONE
    }

}