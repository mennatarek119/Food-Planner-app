package com.example.hmnm.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.MealDetailsActivity
import com.example.hmnm.MealPlanFragment
import com.example.hmnm.Model.Meal
import com.example.hmnm.R
import com.example.hmnm.adapter.AllMealsAdapter
import com.example.hmnm.adapter.MealsDaysAdapter
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.database.entities.MealPlan
import com.example.hmnm.network.ApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AllMealsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mealsAdapter: AllMealsAdapter
    private val mealsList = mutableListOf<Meal>()
    private lateinit var database: AppDatabase
    private var selectedDay: String? = null
    private lateinit var mealsDaysAdapter: MealsDaysAdapter
    private val mealPlansList = mutableListOf<MealPlan>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_meals)
        supportActionBar?.hide()

        database = AppDatabase.getDatabase(this)

        val searchView = findViewById<SearchView>(R.id.searchView)

        val searchPlate = searchView.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate?.setBackgroundColor(Color.TRANSPARENT)
        val searchEditFrame = searchView.findViewById<View>(androidx.appcompat.R.id.search_edit_frame)
        searchEditFrame?.setBackgroundColor(Color.TRANSPARENT)
        val searchBar = searchView.findViewById<View>(androidx.appcompat.R.id.search_bar)
        searchBar?.setBackgroundColor(Color.TRANSPARENT)

        recyclerView = findViewById(R.id.rvMeals)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        mealsAdapter = AllMealsAdapter(
            mealsList,
            onBookmarkClick = { meal -> handleBookmarkClick(meal) },
            onItemClick = { meal -> handleMealAddition(meal) }, onViewClick = { meal -> handleMealView(meal) }
        )
        selectedDay = intent.getStringExtra("selected_day")
        recyclerView.adapter = mealsAdapter
        mealsDaysAdapter = MealsDaysAdapter(mutableListOf(), onRemoveClick = {})

        fetchAllMeals()
        loadFavorites()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMealByName(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun handleRemoveMeal(mealPlan: MealPlan) {
        lifecycleScope.launch {
            database.mealPlanDao().deleteMeal(mealPlan)
            mealPlansList.remove(mealPlan)
            mealsDaysAdapter.updateMeals(mealPlansList)
            Toast.makeText(this@AllMealsActivity, "تم إزالة ${mealPlan.mealName} من $selectedDay", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleFavoriteClick(mealPlan: MealPlan) {
        // منطق المفضلة هنا إذا لزم الأمر
    }

    private fun fetchAllMeals() {
        lifecycleScope.launch {
            try {
                val categoriesResponse = ApiClient.retrofitService.getCategories()

                categoriesResponse.categories.forEach { category ->
                    val mealsResponse = ApiClient.retrofitService.getMealsByCategory(category.strCategory)

                    mealsResponse.meals.forEach { meal ->
                        meal.isFavorite = database.favoriteDao().isMealFavorite(meal.idMeal) > 0
                    }

                    mealsList.addAll(mealsResponse.meals)
                }
                mealsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error fetching meals: ${e.message}")
            }
        }
    }
    private fun handleMealView(meal: Meal) {
        val intent = Intent(this, MealDetailsActivity::class.java).apply {
            putExtra("MEAL_ID", meal.idMeal)
            putExtra("MEAL_RATING", 3.5f) // حطي تقييم مبدئي أو حسب ما متوفر
            putExtra("MEAL_PREP_TIME", 30) // وقت تحضير مبدئي
        }
        startActivity(intent)
    }


    private fun searchMealByName(query: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.retrofitService.searchMealByName(query)
                mealsList.clear()

                response.meals?.forEach { meal ->
                    meal.isFavorite = database.favoriteDao().isMealFavorite(meal.idMeal) > 0
                    mealsList.add(meal)
                }

                mealsAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Error searching meal: ${e.message}")
            }
        }
    }

    private fun handleBookmarkClick(meal: Meal) {
        lifecycleScope.launch {
            // الحصول على userId الحالي من FirebaseAuth
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId == null) {
                // إذا لم يكن هناك userId، يظهر رسالة خطأ
                Toast.makeText(this@AllMealsActivity, "لم يتم العثور على المستخدم", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val isFavorite = database.favoriteDao().isMealFavorite(meal.idMeal) > 0

            if (isFavorite) {
                database.favoriteDao().deleteFavoriteById(meal.idMeal)
                removeMealFromFirebase(meal)
            } else {
                val favoriteMeal = FavoriteMeal(meal.idMeal, userId, meal.strMeal, meal.strMealThumb)
                database.favoriteDao().addFavorite(favoriteMeal)
                addMealToFirebase(favoriteMeal)
            }

            meal.isFavorite = !isFavorite
            mealsAdapter.notifyDataSetChanged()
        }
    }


    private fun addMealToFirebase(favoriteMeal: FavoriteMeal) {
        val userId = getCurrentUserId()
        val favoriteMealRef = FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(userId)
            .collection("meals")
            .document(favoriteMeal.idMeal)

        // بناء الخريطة يدويًا
        val mealMap = hashMapOf(
            "idMeal" to favoriteMeal.idMeal,
            "strMeal" to favoriteMeal.strMeal,
            "strMealThumb" to favoriteMeal.strMealThumb,
        )

        // إرسال البيانات إلى Firebase
        favoriteMealRef.set(mealMap)
    }


    private fun removeMealFromFirebase(meal: Meal) {
        val userId = getCurrentUserId()
        val favoriteMealRef = FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(userId)
            .collection("meals")
            .document(meal.idMeal)

        favoriteMealRef.delete()
    }

    fun getCurrentUserId(): String {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid ?: "" // إرجاع userId إذا كان موجودًا، وإلا إرجاع قيمة فارغة
    }

    private fun handleMealAddition(meal: Meal) {
        lifecycleScope.launch {
            selectedDay?.let { day ->
                val userId = getCurrentUserId()
                val mealPlan = MealPlan(
                    day = day,
                    mealId = meal.idMeal,
                    mealName = meal.strMeal,
                    mealThumb = meal.strMealThumb,
                    userId = userId
                )
                database.mealPlanDao().insertMeal(mealPlan)

                // إضافة الوجبة إلى Firebase
                val mealPlanRef = FirebaseFirestore.getInstance()
                    .collection("mealPlans")
                    .document(userId)
                    .collection("plans")
                    .document(meal.idMeal)

                val mealMap = hashMapOf(
                    "mealId" to meal.idMeal,
                    "mealName" to meal.strMeal,
                    "mealThumb" to meal.strMealThumb,
                    "day" to day,
                )
                Log.d("MealPlans", "MealMap: $mealMap") // تحقق من القيم قبل الإرسال
                mealPlanRef.set(mealMap)

                // تحميل بيانات الوجبات المخصصة لهذا اليوم
                val mealPlans = database.mealPlanDao().getMealsForDay(day)
                mealPlansList.clear()
                mealPlansList.addAll(mealPlans)

                val mealPlanFragment = supportFragmentManager.findFragmentByTag("MEAL_PLAN_FRAGMENT") as? MealPlanFragment
                mealPlanFragment?.loadMealsForAllDays()

                Log.d("MealPlans", "Added meal to $day: $mealPlan")
                Toast.makeText(this@AllMealsActivity, "تمت إضافة ${meal.strMeal} إلى $day", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this@AllMealsActivity, "لم يتم تحديد اليوم", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            val favoriteMeals = database.favoriteDao().getFavorites().map { it.idMeal }.toSet()
            mealsList.forEach { meal ->
                meal.isFavorite = favoriteMeals.contains(meal.idMeal)
            }
            mealsAdapter.notifyDataSetChanged()
        }
    }
}
