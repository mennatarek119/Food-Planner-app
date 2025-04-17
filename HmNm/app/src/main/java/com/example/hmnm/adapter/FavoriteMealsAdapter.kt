import android.app.AlertDialog
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
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class FavoriteAdapter(
    private var mealList: MutableList<FavoriteMeal>, 
    private val onDeleteFavorite: (FavoriteMeal) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_recipe_inner, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val favoriteMeal = mealList[position]
        holder.bind(favoriteMeal)
    }

    override fun getItemCount(): Int = mealList.size

    fun updateItems(newMeals: List<FavoriteMeal>) {
        mealList = newMeals.toMutableList() 
        notifyDataSetChanged()
    }

    inner class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealNameTextView: TextView = itemView.findViewById(R.id.recipe_name)
        private val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_icon)
        private val favoritemealImage: ImageView = itemView.findViewById(R.id.recipe_image)
        private val prepTime: TextView = itemView.findViewById(R.id.preparation_time)
        private val rating: RatingBar = itemView.findViewById(R.id.rating_bar)
        private val ingredientsCount: TextView = itemView.findViewById(R.id.add_ingredients_button)

        fun bind(favoriteMeal: FavoriteMeal) {
            mealNameTextView.text = favoriteMeal.strMeal

            Glide.with(itemView.context)
                .load(favoriteMeal.strMealThumb)
                .placeholder(R.drawable.placholder)
                .into(favoritemealImage)

            val randomPrepTime = Random.nextInt(10, 61) 
            val randomRating = Random.nextFloat() * (5f - 3f) + 3f 

            prepTime.text = "$randomPrepTime min"
            rating.rating = randomRating


            CoroutineScope(Dispatchers.Main).launch {
                val isFavorite = isMealFavorite(favoriteMeal.idMeal) || isMealFavoriteInFirestore(favoriteMeal.idMeal)
                favoriteMeal.isFavorite = isFavorite
                updateBookmarkIcon(favoriteMeal)
            }

            bookmarkIcon.setOnClickListener {
                val newFavoriteStatus = !favoriteMeal.isFavorite

                if (!newFavoriteStatus) {
                    val builder = AlertDialog.Builder(itemView.context)
                    val title = SpannableString("Remove favorite")
                    title.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.teal_700)), 0, title.length, 0)
                    builder.setTitle(title)

                    builder.setMessage("Are you sure you want to remove this meal from your favorites?")
                    builder.setPositiveButton("Yes") { _, _ ->
                        favoriteMeal.isFavorite = false
                        removeItem(adapterPosition)
                        removeFromDatabase(favoriteMeal)
                        removeFromFirestore(favoriteMeal)
                        updateBookmarkIcon(favoriteMeal)
                    }
                    builder.setNegativeButton("Cancel", null)

                    val alertDialog = builder.create()
                    alertDialog.setOnShowListener {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            ?.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
                        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            ?.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray))
                    }
                    alertDialog.show()
                } else {
                    favoriteMeal.isFavorite = true
                    addToDatabase(favoriteMeal)
                    addToFirestore(favoriteMeal)
                    updateBookmarkIcon(favoriteMeal)
                }
            }



            itemView.setOnClickListener {
                val intent = Intent(itemView.context, MealDetailsActivity::class.java).apply {
                    putExtra("MEAL_ID", favoriteMeal.idMeal)
                    putExtra("MEAL_NAME", favoriteMeal.strMeal)
                    putExtra("MEAL_IMAGE", favoriteMeal.strMealThumb)
                    putExtra("MEAL_PREP_TIME", randomPrepTime)
                    putExtra("MEAL_RATING", randomRating)

                }
                itemView.context.startActivity(intent)
            }
        }

        private fun updateBookmarkIcon(favoriteMeal: FavoriteMeal) {
            if (favoriteMeal.isFavorite) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmarked_foreground)
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark)
            }
        }
        fun getCurrentUserId(): String {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            return firebaseUser?.uid ?: ""
        }

        private suspend fun isMealFavorite(mealId: String): Boolean {
           
            val db = AppDatabase.getDatabase(itemView.context)
            return db.favoriteDao().getFavoriteById(mealId) != null
        }
        private suspend fun isMealFavoriteInFirestore(mealId: String): Boolean {
            val userId = getCurrentUserId() 
            val favoriteMealRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(userId)
                .collection("meals")
                .document(mealId)

            return try {
                val docSnapshot = favoriteMealRef.get().await()
                docSnapshot.exists()
            } catch (e: Exception) {
                false
            }
        }

        private fun addToDatabase(favoriteMeal: FavoriteMeal) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(itemView.context)
                db.favoriteDao().addFavorite(favoriteMeal) 
            }
        }

        private fun removeFromDatabase(favoriteMeal: FavoriteMeal) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(itemView.context)
                db.favoriteDao().deleteFavorite(favoriteMeal) 
            }
        }
        private fun addToFirestore(favoriteMeal: FavoriteMeal) {
            val userId = getCurrentUserId()
            val favoriteMealRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(userId)
                .collection("meals")
                .document(favoriteMeal.idMeal)

            val mealMap = hashMapOf(
                "idMeal" to favoriteMeal.idMeal,
                "strMeal" to favoriteMeal.strMeal,
                "strMealThumb" to favoriteMeal.strMealThumb
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    favoriteMealRef.set(mealMap).await()
                } catch (e: Exception) {
                   
                }
            }
        }
        private fun removeFromFirestore(favoriteMeal: FavoriteMeal) {
            val userId = getCurrentUserId() 
            val favoriteMealRef = FirebaseFirestore.getInstance()
                .collection("favorites")
                .document(userId)
                .collection("meals")
                .document(favoriteMeal.idMeal)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    favoriteMealRef.delete().await()
                } catch (e: Exception) {
                   
                }
            }
        }

        private fun removeItem(position: Int) {
            mealList.removeAt(position) 
            notifyItemRemoved(position) 
        }
    }

}
