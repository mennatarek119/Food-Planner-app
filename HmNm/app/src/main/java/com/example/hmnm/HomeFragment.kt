package com.example.hmnm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.Model.Meal
import com.example.hmnm.adapter.AreaAdapter
import com.example.hmnm.adapter.CategoryAdapter
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.dao.FavoriteDao
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.network.ApiClient
import com.example.hmnm.utils.getFlagResId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var areasRecyclerView: RecyclerView
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var areasAdapter: AreaAdapter
    private lateinit var categoriesAdapter: CategoryAdapter
    private lateinit var randomMealImageView: ImageView
    private lateinit var randomMealNameTextView: TextView
    private lateinit var ingredints: TextView
    private lateinit var randomMealIngredients: Button
    private lateinit var randomMealTimeTextView: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var BookmarkIcon: ImageView
    private lateinit var database: AppDatabase
    private lateinit var favoriteMealDao: FavoriteDao


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        areasRecyclerView = rootView.findViewById(R.id.mealsRecyclerView2)
        areasRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        areasAdapter = AreaAdapter(emptyList()) { areaName ->
            val intent = Intent(requireContext(), MealsActivity::class.java).apply {
                putExtra("NAME", areaName)
                putExtra("IS_CATEGORY", false)
            }
            startActivity(intent)
        }
        areasRecyclerView.adapter = areasAdapter

        categoriesRecyclerView = rootView.findViewById(R.id.mealsRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoriesAdapter = CategoryAdapter(emptyList()) { categoryName ->
            val intent = Intent(requireContext(), MealsActivity::class.java).apply {
                putExtra("NAME", categoryName)
                putExtra("IS_CATEGORY", true)
            }
            startActivity(intent)
        }
        categoriesRecyclerView.adapter = categoriesAdapter

        randomMealImageView = rootView.findViewById(R.id.randomMealImageView)
        randomMealNameTextView = rootView.findViewById(R.id.randomMealNameTextView)
        ingredints = rootView.findViewById(R.id.ingredints)
        randomMealIngredients = rootView.findViewById(R.id.randomMealIngredients)
        randomMealTimeTextView = rootView.findViewById(R.id.randomMealTimeTextView)
        ratingBar = rootView.findViewById(R.id.rating_bar)
        BookmarkIcon = rootView.findViewById(R.id.bookmark_icon)

        database = AppDatabase.getDatabase(requireContext())
        favoriteMealDao = database.favoriteDao()


        loadAreas()
        loadCategories()
        loadRandomMeal()



        return rootView
    }

    private fun loadAreas() {
        lifecycleScope.launch {
            try {
                val areaResponse = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getAreas()
                }
                val areas = areaResponse.meals
                areasAdapter.updateItems(areas.map { area -> area.strArea to getFlagResId(area.strArea) })
                areasRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                areasRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categoryResponse = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getCategories()
                }
                val categories = categoryResponse.categories
                categoriesAdapter.updateItems(categories)
                categoriesRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                categoriesRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun loadRandomMeal() {
        lifecycleScope.launch {
            try {
                val mealResponse = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getRandomMeal()
                }
                val meals = mealResponse.meals
                if (meals.isNotEmpty()) {
                    val randomMeal = meals[0]

                    randomMealNameTextView.text = randomMeal.strMeal
                    Glide.with(this@HomeFragment)
                        .load(randomMeal.strMealThumb)
                        .into(randomMealImageView)

                    val ingredientCount = getIngredientCount(randomMeal)
                    ingredints.text = "$ingredientCount INGREDIENTS"

                    val prepTime = (10..60).random() 
                    val rating = (3..5).random() + (0..9).random() / 10f 

                    randomMealTimeTextView.text = "$prepTime min"
                    ratingBar.rating = rating

                    val isFavorite = withContext(Dispatchers.IO) {
                        favoriteMealDao.isMealFavorite(randomMeal.idMeal) > 0
                    }
                    updateBookmarkIcon(isFavorite)

                    BookmarkIcon.setOnClickListener {
                        lifecycleScope.launch {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId == null) {
                                Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                                return@launch
                            }

                            withContext(Dispatchers.IO) {
                                if (favoriteMealDao.isMealFavorite(randomMeal.idMeal) > 0) {
                                    favoriteMealDao.deleteFavoriteById(randomMeal.idMeal)
                                } else {
                                    favoriteMealDao.addFavorite(
                                        FavoriteMeal(
                                            idMeal = randomMeal.idMeal,
                                            strMeal = randomMeal.strMeal,
                                            strMealThumb = randomMeal.strMealThumb,
                                            userId = userId
                                        )
                                    )
                                }
                            }

                            val firestoreDb = FirebaseFirestore.getInstance()
                            val mealRef = firestoreDb.collection("favorites")
                                .document(userId)
                                .collection("meals")
                                .document(randomMeal.idMeal)

                            val isFavoriteNow = withContext(Dispatchers.IO) {
                                favoriteMealDao.isMealFavorite(randomMeal.idMeal) > 0
                            }

                            if (isFavoriteNow) {
                                mealRef.set(
                                    mapOf(
                                        "idMeal" to randomMeal.idMeal,
                                        "strMeal" to randomMeal.strMeal,
                                        "strMealThumb" to randomMeal.strMealThumb,
                                        "userId" to userId
                                    )
                                ).addOnSuccessListener {
                                    updateBookmarkIcon(true) 
                                }.addOnFailureListener {
                                    Toast.makeText(requireContext(), "Error saving to Firestore", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                mealRef.delete().addOnSuccessListener {
                                    updateBookmarkIcon(false) 
                                }.addOnFailureListener {
                                    Toast.makeText(requireContext(), "Error removing from Firestore", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }

                    randomMealIngredients.setOnClickListener {
                        val intent = Intent(requireContext(), MealDetailsActivity::class.java).apply {
                            putExtra("MEAL_ID", randomMeal.idMeal)
                            putExtra("MEAL_NAME", randomMeal.strMeal)
                            putExtra("MEAL_IMAGE", randomMeal.strMealThumb)
                            putExtra("MEAL_AREA", randomMeal.strArea)
                            putExtra("MEAL_INSTRUCTIONS", randomMeal.strInstructions)
                            putExtra("MEAL_YOUTUBE", randomMeal.strYoutube)
                            putExtra("MEAL_INGREDIENTS", getIngredientsText(randomMeal))
                            putExtra("MEAL_PREP_TIME", prepTime)  
                            putExtra("MEAL_RATING", rating)
                        }
                        startActivity(intent)
                    }

                } else {
                    randomMealNameTextView.text = "No random meal found"
                }
            } catch (e: Exception) {
                randomMealNameTextView.text = "Error loading meal"
            }
        }
    }

    private fun updateBookmarkIcon(isFavorite: Boolean) {
        if (isFavorite) {
            BookmarkIcon.setImageResource(R.drawable.ic_bookmarked_foreground)
        } else {
            BookmarkIcon.setImageResource(R.drawable.ic_bookmark)
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


private fun getIngredientsText(meal: Meal): String {
        val ingredients = listOf(
            meal.strIngredient1, meal.strIngredient2, meal.strIngredient3, meal.strIngredient4, meal.strIngredient5,
            meal.strIngredient6, meal.strIngredient7, meal.strIngredient8, meal.strIngredient9, meal.strIngredient10,
            meal.strIngredient11, meal.strIngredient12, meal.strIngredient13, meal.strIngredient14, meal.strIngredient15,
            meal.strIngredient16, meal.strIngredient17, meal.strIngredient18, meal.strIngredient19, meal.strIngredient20
        )
        return ingredients.filterNotNull().filter { it.isNotBlank() }.joinToString("\n")
    }



