package com.example.hmnm

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.Model.Meal
import com.example.hmnm.adapter.IngredientsAdapter
import com.example.hmnm.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MealDetailsActivity : AppCompatActivity() {

    private lateinit var mealImageView: ImageView
    private lateinit var mealNameTextView: TextView
    private lateinit var mealAreaTextView: TextView
    private lateinit var mealInstructionsTextView: TextView
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var webView: WebView
    private var isExpanded = false
    private lateinit var fullInstructions: String



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_details)
        supportActionBar?.hide()

        // استقبال ID الوجبة
        val mealId = intent.getStringExtra("MEAL_ID") ?: return
        val prepTime = intent.getIntExtra("MEAL_PREP_TIME", -1)
        val rating = intent.getFloatExtra("MEAL_RATING", -1.0f)


        // ربط العناصر بالتصميم
        mealImageView = findViewById(R.id.mealImageView)
        mealNameTextView = findViewById(R.id.mealNameTextView)
        mealAreaTextView = findViewById(R.id.mealAreaTextView)
        mealInstructionsTextView = findViewById(R.id.mealInstructionsTextView)
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView)
        webView = findViewById(R.id.webView)

        val ratingBar: RatingBar = findViewById(R.id.rating_bar)
        val prepTimeTextView: TextView = findViewById(R.id.prepTimeTextView)

        ratingBar.rating = rating
        prepTimeTextView.text = "${prepTime} min"


        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webView.webChromeClient = WebChromeClient()

        loadMealDetails(mealId)
    }

    private fun loadMealDetails(mealId: String) {
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    ApiClient.retrofitService.getMealDetails(mealId)
                }
                val meal = response.meals.firstOrNull()
                meal?.let { updateUI(it) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setInstructionsText() {
        val coloredPart = if (isExpanded) " Less" else "... More"
        val baseText = if (isExpanded) fullInstructions else fullInstructions.take(200)

        val finalText = baseText + coloredPart
        val spannable = SpannableString(finalText)

        val start = finalText.indexOf(coloredPart)
        val end = start + coloredPart.length

        val colorSpan = ForegroundColorSpan(ContextCompat.getColor(this, R.color.teal_700))
        spannable.setSpan(colorSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mealInstructionsTextView.text = spannable
    }

    private fun updateUI(meal: Meal) {
        mealNameTextView.text = meal.strMeal
        mealAreaTextView.text = "Country: ${meal.strArea}"
        fullInstructions = meal.strInstructions ?: ""
        val shortText = if (fullInstructions.length > 200) {
            fullInstructions.take(200) + "... More"
        } else {
            fullInstructions
        }

        mealInstructionsTextView.text = shortText
        mealInstructionsTextView.setOnClickListener {
            isExpanded = !isExpanded
            setInstructionsText()
        }



        Glide.with(this)
            .load(meal.strMealThumb)
            .into(mealImageView)

        meal.strYoutube?.let { url ->
            val videoUrl = "https://www.youtube.com/embed/${getYouTubeVideoId(url)}"
            webView.loadUrl(videoUrl)
        }

        val ingredientsList = getIngredientsList(meal)

        // تمرير دالة عند الضغط على عنصر المكون
        val ingredientsAdapter = IngredientsAdapter(ingredientsList) { ingredientName ->
            val intent = Intent(this, MealsActivity::class.java).apply {
                putExtra("NAME", ingredientName)
                putExtra("IS_CATEGORY", false)   // لتحديد أن المصدر ليس تصنيفًا
                putExtra("IS_INGREDIENT", true)  // تحديد أن البحث يتم حسب المكون
            }
            startActivity(intent)
        }

        ingredientsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        ingredientsRecyclerView.adapter = ingredientsAdapter
    }



    private fun getIngredientsList(meal: Meal): List<Pair<String, String>> {
        val ingredients = listOf(
            meal.strIngredient1, meal.strIngredient2, meal.strIngredient3, meal.strIngredient4,
            meal.strIngredient5, meal.strIngredient6, meal.strIngredient7, meal.strIngredient8,
            meal.strIngredient9, meal.strIngredient10, meal.strIngredient11, meal.strIngredient12,
            meal.strIngredient13, meal.strIngredient14, meal.strIngredient15, meal.strIngredient16,
            meal.strIngredient17, meal.strIngredient18, meal.strIngredient19, meal.strIngredient20
        ).filterNotNull().filter { it.isNotBlank() }

        return ingredients.map { ingredient ->
            val imageUrl = "https://www.themealdb.com/images/ingredients/$ingredient.png"
            Pair(ingredient, imageUrl)
        }
    }

    // دالة لاستخراج ID الفيديو من رابط YouTube
    private fun getYouTubeVideoId(url: String): String {
        val pattern = "https?://(?:www\\.)?youtube\\.com/watch\\?v=([\\w-]+)"
        val regex = Regex(pattern)
        val matchResult = regex.find(url)
        return matchResult?.groups?.get(1)?.value ?: ""
    }
}
