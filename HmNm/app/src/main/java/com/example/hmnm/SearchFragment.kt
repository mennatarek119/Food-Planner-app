package com.example.hmnm

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.Model.Meal
import com.example.hmnm.adapter.AreaAdapter
import com.example.hmnm.adapter.CategoryAdapter
import com.example.hmnm.adapter.IngredientsAdapter
import com.example.hmnm.adapter.MealsAdapter
import com.example.hmnm.network.ApiClient
import com.example.hmnm.utils.getFlagResId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchFragment : Fragment() {

    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var categoriesRecyclerView: RecyclerView
    private lateinit var countryRecyclerView: RecyclerView  

    private lateinit var ingredientsAdapter: IngredientsAdapter
    private lateinit var categoriesAdapter: CategoryAdapter
    private lateinit var countryAdapter: AreaAdapter  
    private lateinit var mealAdapter: MealsAdapter

    private lateinit var viewAllButtonIngredients: Button
    private lateinit var viewAllButtonCategories: Button
    private lateinit var viewAllButtonCountries: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_search, container, false)

        ingredientsRecyclerView = rootView.findViewById(R.id.ingredientRecyclerView)
        ingredientsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        ingredientsAdapter = IngredientsAdapter(emptyList()) { ingredientName ->
            openMealsActivity(ingredientName, isCategory = false)
        }
        ingredientsRecyclerView.adapter = ingredientsAdapter

        categoriesRecyclerView = rootView.findViewById(R.id.categoryRecyclerView)
        categoriesRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        categoriesAdapter = CategoryAdapter(emptyList()) { categoryName ->
            openMealsActivity(categoryName, isCategory = true)
        }
        categoriesRecyclerView.adapter = categoriesAdapter

        mealsRecyclerView = rootView.findViewById(R.id.mealRecyclerview)
        mealsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealAdapter = MealsAdapter(emptyList(),requireContext())
        mealsRecyclerView.adapter = mealAdapter

        countryRecyclerView = rootView.findViewById(R.id.countryRecyclerView)
        countryRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        countryAdapter = AreaAdapter(emptyList()) { countryName ->
            openMealsActivity(countryName, isCategory = false, isArea = true) 
        }

        countryRecyclerView.adapter = countryAdapter

        viewAllButtonIngredients = rootView.findViewById(R.id.viewAllButtonIngredients)

        val content = SpannableString("VIEW ALL")
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        viewAllButtonIngredients.text = content
        viewAllButtonIngredients.setOnClickListener {
            openIngredientsActivity()
        }

        viewAllButtonCategories = rootView.findViewById(R.id.viewAllButtonCategories)
        viewAllButtonCategories.text = content
        viewAllButtonCategories.setOnClickListener {
            openCategoriesActivity(CategoriesOrAreasActivity.TYPE_CATEGORY)
        }

        viewAllButtonCountries = rootView.findViewById(R.id.viewAllButtonCountry)
        viewAllButtonCountries.text = content
        viewAllButtonCountries.setOnClickListener {
            openCategoriesActivity(CategoriesOrAreasActivity.TYPE_COUNTRY)
        }

        loadIngredients()
        loadCategories()
        loadCountries()
        loadRandomMeals()

        return rootView
    }

    private fun loadIngredients() {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getIngredients()
                }
                val limitedIngredients = response.meals?.take(5) ?: emptyList()
                ingredientsAdapter.updateItems(limitedIngredients.map { it.strIngredient to it.ingredientsImage })
                ingredientsRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                showErrorMessage("Error loading ingredients: ${e.message}")
            }
        }
    }


    private fun loadCategories() {
        lifecycleScope.launch {
            try {
                val categoryResponse = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getCategories()
                }
                categoriesAdapter.updateItems(categoryResponse.categories.take(5) )
                categoriesRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                categoriesRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun loadCountries() {
        lifecycleScope.launch {
            try {
                val countryResponse = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getAreas()
                }

                val countryList = countryResponse.meals.take(5).mapNotNull {
                    val flagResId = getFlagResId(it.strArea)
                    if (flagResId != 0) Pair(it.strArea, flagResId) else null
                }

                countryAdapter.updateItems(countryList)  
                countryRecyclerView.visibility = View.VISIBLE

            } catch (e: Exception) {
                showErrorMessage("Error loading countries: ${e.message}")
                countryRecyclerView.visibility = View.GONE
            }
        }
    }

    private fun loadRandomMeals() {
        lifecycleScope.launch {
            try {
                val meals = mutableListOf<Meal>()

                repeat(5) {
                    val mealResponse = withContext(Dispatchers.IO) {
                        ApiClient.retrofitService.getRandomMeal()
                    }

                    mealResponse.meals?.firstOrNull()?.let { meal ->
                        val updatedMeal = meal.copy(ingredientCount = getIngredientCount(meal))
                        meals.add(updatedMeal)
                    }
                }

                if (meals.isNotEmpty()) {
                    mealAdapter.updateItems(meals)
                    mealsRecyclerView.visibility = View.VISIBLE
                } else {
                    showErrorMessage("No random meals found.")
                }
            } catch (e: Exception) {
                showErrorMessage("Error loading random meals: ${e.message}")
            }
        }
    }

    private fun showErrorMessage(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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

    private fun openMealsActivity(name: String, isCategory: Boolean, isArea: Boolean = false) {
        val intent = Intent(requireContext(), MealsActivity::class.java)
        intent.putExtra("NAME", name)
        intent.putExtra("IS_CATEGORY", isCategory)
        intent.putExtra("IS_INGREDIENT", !isCategory && !isArea) 
        intent.putExtra("IS_AREA", isArea)
        startActivity(intent)
    }


    private fun openIngredientsActivity() {
        val intent = Intent(requireContext(), IngredientsActivity::class.java)
        startActivity(intent)
    }

    private fun openCategoriesActivity(type: String) {
        val intent = Intent(requireContext(), CategoriesOrAreasActivity::class.java)
        intent.putExtra(CategoriesOrAreasActivity.EXTRA_TYPE, type)
        startActivity(intent)
    }



}
