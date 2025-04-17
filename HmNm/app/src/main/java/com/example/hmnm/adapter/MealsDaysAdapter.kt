package com.example.hmnm.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hmnm.MealDetailsActivity
import com.example.hmnm.R
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.example.hmnm.database.entities.MealPlan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MealsDaysAdapter(
    private var meals: MutableList<MealPlan>,
    private val onRemoveClick: (MealPlan) -> Unit
) : RecyclerView.Adapter<MealsDaysAdapter.MealsDaysViewHolder>() {

    class MealsDaysViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTextView: TextView = itemView.findViewById(R.id.mealTextView)
        val removeButton: Button = itemView.findViewById(R.id.removeButton)
        val mealImageView: ImageView = itemView.findViewById(R.id.mealImageView)
        val favButton: ImageView = itemView.findViewById(R.id.FavButton)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBar)
        val prepTimeTextView: TextView = itemView.findViewById(R.id.prepTimeTextView)

        fun bind(meal: MealPlan, position: Int, adapter: MealsDaysAdapter) {
            mealTextView.text = meal.mealName

            Glide.with(itemView.context)
                .load(meal.mealThumb)
                .apply(RequestOptions().placeholder(R.drawable.placholder).centerCrop())
                .into(mealImageView)

            val context = itemView.context
            val db = AppDatabase.getDatabase(context)
            val favoriteDao = db.favoriteDao()

            val randomRating = Random.nextFloat() * (5 - 1) + 1 
            val randomPrepTime = Random.nextInt(10, 60) 

            ratingBar.rating = randomRating
            prepTimeTextView.text = "$randomPrepTime min"

            CoroutineScope(Dispatchers.IO).launch {
                val isFavLocal = favoriteDao.getFavoriteById(meal.mealId) != null
                meal.isFavorite = isFavLocal

                val userId = getCurrentUserId() 
                val favoriteMealRef = FirebaseFirestore.getInstance()
                    .collection("favorites")
                    .document(userId)
                    .collection("meals")
                    .document(meal.mealId)

                val isFavFirestore = try {
                    val docSnapshot = favoriteMealRef.get().await() 
                    docSnapshot.exists()
                } catch (e: Exception) {
                    false 
                }

                meal.isFavorite = isFavLocal || isFavFirestore

                withContext(Dispatchers.Main) {
               
                    favButton.setImageResource(
                        if (meal.isFavorite) R.drawable.ic_bookmarked_foreground else R.drawable.ic_bookmark
                    )
                }
            }


            itemView.setOnClickListener {
                val intent = Intent(context, MealDetailsActivity::class.java).apply {
                    putExtra("MEAL_ID", meal.mealId)
                    putExtra("MEAL_NAME", meal.mealName)
                    putExtra("MEAL_THUMB", meal.mealThumb)
                    putExtra("MEAL_RATING", randomRating)
                    putExtra("MEAL_PREP_TIME", randomPrepTime)
                }
                context.startActivity(intent)
            }

            favButton.setOnClickListener {
                toggleFavorite(meal, adapter)
            }

            removeButton.setOnClickListener {
                val builder = android.app.AlertDialog.Builder(itemView.context)

                val title = android.text.SpannableString("Remove Meal")
                title.setSpan(
                    android.text.style.ForegroundColorSpan(
                        androidx.core.content.ContextCompat.getColor(itemView.context, R.color.teal_700)
                    ), 0, title.length, 0
                )
                builder.setTitle(title)

                builder.setMessage("Are you sure you want to remove this meal from your plan?")
                builder.setPositiveButton("Yes") { _, _ ->
                    CoroutineScope(Dispatchers.IO).launch {
                        db.mealPlanDao().deleteMeal(meal)

                        val userId = getCurrentUserId()
                        val mealPlanRef = FirebaseFirestore.getInstance()
                            .collection("mealPlans")
                            .document(userId)
                            .collection("plans")
                            .document(meal.mealId)

                        mealPlanRef.delete()

                        withContext(Dispatchers.Main) {
                            adapter.removeMeal(meal)
                        }
                    }
                }

                builder.setNegativeButton("Cancel", null)

                val alertDialog = builder.create()
                alertDialog.setOnShowListener {
                    alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                        ?.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.red))
                    alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                        ?.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.context, R.color.gray))
                }

                alertDialog.show()
            }


        }

        fun getCurrentUserId(): String {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            return firebaseUser?.uid ?: "" 
        }


        private fun toggleFavorite(meal: MealPlan, adapter: MealsDaysAdapter) {
            val context = itemView.context
            val db = AppDatabase.getDatabase(context)
            val favoriteDao = db.favoriteDao()
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val existingFavorite = favoriteDao.getFavoriteById(meal.mealId)

                val firestoreRef = FirebaseFirestore.getInstance()
                    .collection("favorites")
                    .document(userId)
                    .collection("meals")
                    .document(meal.mealId)

                if (existingFavorite == null) {
                    val favorite = FavoriteMeal(
                        idMeal = meal.mealId,
                        strMeal = meal.mealName,
                        strMealThumb = meal.mealThumb,
                        userId = userId
                    )
                    favoriteDao.addFavorite(favorite)

                    val mealMap = hashMapOf(
                        "idMeal" to meal.mealId,
                        "strMeal" to meal.mealName,
                        "strMealThumb" to meal.mealThumb,
                        "userId" to userId
                    )
                    firestoreRef.set(mealMap)

                    meal.isFavorite = true

                    withContext(Dispatchers.Main) {
                        adapter.notifyItemChanged(adapterPosition)
                    }

                } else {
                    withContext(Dispatchers.Main) {
                        val builder = android.app.AlertDialog.Builder(context)

                        val title = android.text.SpannableString("Remove favorite")
                        title.setSpan(
                            android.text.style.ForegroundColorSpan(
                                androidx.core.content.ContextCompat.getColor(context, R.color.teal_700)
                            ), 0, title.length, 0
                        )
                        builder.setTitle(title)

                        builder.setMessage("Do you want to remove this meal from your favorites?")
                        builder.setPositiveButton("Yes") { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                favoriteDao.deleteFavorite(existingFavorite)
                                firestoreRef.delete()
                                meal.isFavorite = false

                                withContext(Dispatchers.Main) {
                                    adapter.notifyItemChanged(adapterPosition)
                                }
                            }
                        }
                        builder.setNegativeButton("Cancel", null)

                        val alertDialog = builder.create()
                        alertDialog.setOnShowListener {
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
                                ?.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.red))
                            alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                                ?.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray))
                        }

                        alertDialog.show()
                    }
                }
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealsDaysViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.meal_item, parent, false)
        return MealsDaysViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MealsDaysViewHolder, position: Int) {
        holder.bind(meals[position], position, this)
    }

    override fun getItemCount() = meals.size

    fun removeMeal(meal: MealPlan) {
        val position = meals.indexOf(meal)
        if (position != -1) {
            meals.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun updateMeals(newMeals: List<MealPlan>) {
        meals.clear()
        meals.addAll(newMeals)
        notifyDataSetChanged()
    }
}
