package com.example.hmnm

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.adapter.DaysAdapter
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.MealPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MealPlanFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DaysAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_plan, container, false)

        recyclerView = view.findViewById(R.id.rvDays)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        database = AppDatabase.getDatabase(requireContext())

        loadMealsForAllDays()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadMealsForAllDays() 
    }

    private fun getUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid 
    }

    fun loadMealsForAllDays() {
        val userId = getUserId() 

        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val mealPlansRef = FirebaseFirestore.getInstance()
                .collection("mealPlans") 
                .document(userId) 
                .collection("plans") 

            mealPlansRef.get()
                .addOnSuccessListener { documents ->
                    val allMeals = mutableListOf<MealPlan>()
                    for (document in documents) {
                        val mealPlan = document.toObject(MealPlan::class.java)
                        Log.d("MealPlans", "Loaded Meal: ${mealPlan.day}, ${mealPlan.mealName}, ${mealPlan.mealThumb}, ${mealPlan.mealId}")
                        allMeals.add(mealPlan)
                    }

                    val mealsMap = allMeals.groupBy { it.day }

                    val daysList = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

                    val mealsListMap = mutableMapOf<String, List<MealPlan>>()
                    daysList.forEach { day ->
                        mealsListMap[day] = mealsMap[day] ?: emptyList() 
                    }

                    adapter = DaysAdapter(daysList, mealsListMap, requireContext())
                    recyclerView.adapter = adapter
                }
                .addOnFailureListener { e ->
                    Log.e("MealPlans", "Error loading meal plans from Firestore: ${e.message}")
                }
        }
    }

}
