package com.example.hmnm.adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.MealDetailsActivity
import com.example.hmnm.Model.Meal
import com.example.hmnm.R
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class MealsAdapter(
    private var meals: List<Meal>,
    private val context: Context
) : RecyclerView.Adapter<MealsAdapter.MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_inner, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    fun updateItems(newMeals: List<Meal>) {
        meals = newMeals
        notifyDataSetChanged()
    }

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealNameTextView: TextView = itemView.findViewById(R.id.recipe_name)
        private val mealImageView: ImageView = itemView.findViewById(R.id.recipe_image)
        private val addIngredientsButton: MaterialButton = itemView.findViewById(R.id.add_ingredients_button)
        private val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
        private val prepTimeTextView: TextView = itemView.findViewById(R.id.preparation_time)
        private val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_icon)

        fun bind(meal: Meal) {
            mealNameTextView.text = meal.strMeal
            Glide.with(mealImageView.context)
                .load(meal.strMealThumb)
                .into(mealImageView)

            val prepTime = Random.nextInt(10, 61)
            prepTimeTextView.text = "$prepTime min"

            val randomRating = Random.nextDouble(3.0, 5.0).toFloat()
            ratingBar.rating = randomRating

            addIngredientsButton.text = "${meal.ingredientCount} Ingredients"

            // ✅ **التحقق من قاعدة البيانات لمعرفة حالة الوجبة في المفضلة**
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val favoriteDao = db.favoriteDao()
                val isFavorite = favoriteDao.getFavoriteById(meal.idMeal) != null

                meal.isFavorite = isFavorite
                withContext(Dispatchers.Main) {
                    updateBookmarkIcon(meal)
                }
            }

            // ✅ **عند الضغط على الأيقونة، يتم الإضافة أو الحذف من قاعدة البيانات*

            bookmarkIcon.setOnClickListener {
                if (meal.isFavorite) {
                    // عرض نافذة تأكيد إذا كانت الوجبة مفضلة بالفعل
                    val builder = android.app.AlertDialog.Builder(context)

                    // تخصيص النص باستخدام SpannableString لتغيير اللون
                    val titleText = SpannableString("Remove favorite")
                    titleText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.teal_700)), 0, titleText.length, 0)
                    builder.setTitle(titleText)

                    val messageText = SpannableString("Are you sure you want to remove this meal from your favorites?")
                    messageText.setSpan(ForegroundColorSpan(ContextCompat.getColor(context, R.color.black)), 0, messageText.length, 0)
                    builder.setMessage(messageText)

                    builder.setPositiveButton("Yes") { _, _ ->
                        // إذا وافق المستخدم، يتم إزالة الوجبة من المفضلة
                        saveToFavorites(meal)
                    }

                    builder.setNegativeButton("Cancel", null)

                    // تخصيص ألوان الأزرار في النافذة
                    val alertDialog = builder.create()

                    // بعد عرض النافذة، نحدث الأزرار
                    alertDialog.setOnShowListener {
                        alertDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.let {
                            it.setTextColor(ContextCompat.getColor(context, R.color.red))
                        }
                        alertDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.let {
                            it.setTextColor(ContextCompat.getColor(context, R.color.gray))
                        }
                    }

                    alertDialog.show() // تأكد من استدعاء show() بعد تخصيص الأزرار
                } else {
                    // إضافة الوجبة إلى المفضلة مباشرة إذا لم تكن مفضلة
                    saveToFavorites(meal)
                }
            }



            itemView.setOnClickListener {
                val intent = Intent(itemView.context, MealDetailsActivity::class.java).apply {
                    putExtra("MEAL_ID", meal.idMeal)
                    putExtra("MEAL_NAME", meal.strMeal)
                    putExtra("MEAL_IMAGE", meal.strMealThumb)
                    putExtra("MEAL_AREA", meal.strArea)
                    putExtra("MEAL_INSTRUCTIONS", meal.strInstructions)
                    putExtra("MEAL_YOUTUBE", meal.strYoutube)
                    putExtra("MEAL_INGREDIENTS", getIngredientsText(meal))
                    putExtra("MEAL_PREP_TIME", prepTime)
                    putExtra("MEAL_RATING", randomRating)
                }
                itemView.context.startActivity(intent)
            }
        }

        private fun saveToFavorites(meal: Meal) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val favoriteDao = db.favoriteDao()

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val existingFavorite = favoriteDao.getFavoriteByIdAndUser(meal.idMeal, userId)

                if (existingFavorite == null) {
                    // ✅ إضافة للمفضلة في قاعدة البيانات المحلية
                    val favorite = FavoriteMeal(
                        idMeal = meal.idMeal,
                        strMeal = meal.strMeal,
                        strMealThumb = meal.strMealThumb,
                        userId = userId
                    )
                    favoriteDao.addFavorite(favorite)
                    meal.isFavorite = true

                    // ✅ إضافة للمفضلة في Firestore
                    val firestoreDb = FirebaseFirestore.getInstance()
                    firestoreDb.collection("favorites")
                        .document(userId)
                        .collection("meals")
                        .document(meal.idMeal)
                        .set(favorite)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                updateBookmarkIcon(meal) // تحديث الأيقونة
                            }
                        }
                        .addOnFailureListener { e ->
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Error saving to Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // ❌ إزالة من المفضلة في قاعدة البيانات المحلية
                    favoriteDao.deleteFavorite(existingFavorite)
                    meal.isFavorite = false

                    // ❌ إزالة من المفضلة في Firestore
                    val firestoreDb = FirebaseFirestore.getInstance()
                    firestoreDb.collection("favorites")
                        .document(userId)
                        .collection("meals")
                        .document(meal.idMeal)
                        .delete()
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                updateBookmarkIcon(meal) // تحديث الأيقونة
                            }
                        }
                        .addOnFailureListener { e ->
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(context, "Error removing from Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }




        private fun updateBookmarkIcon(meal: Meal) {
            if (meal.isFavorite) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmarked_foreground) // ✅ أيقونة محفوطة
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark) // ❌ أيقونة غير محفوظة (border)
            }
        }

        private fun getIngredientsText(meal: Meal): String {
            val ingredients = listOf(
                meal.strIngredient1, meal.strIngredient2, meal.strIngredient3, meal.strIngredient4,
                meal.strIngredient5, meal.strIngredient6, meal.strIngredient7, meal.strIngredient8,
                meal.strIngredient9, meal.strIngredient10, meal.strIngredient11, meal.strIngredient12,
                meal.strIngredient13, meal.strIngredient14, meal.strIngredient15, meal.strIngredient16,
                meal.strIngredient17, meal.strIngredient18, meal.strIngredient19, meal.strIngredient20
            )
            return ingredients.filterNotNull().filter { it.isNotBlank() }.joinToString("\n")
        }
    }
}
