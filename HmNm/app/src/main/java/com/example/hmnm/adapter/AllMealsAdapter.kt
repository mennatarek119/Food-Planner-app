package com.example.hmnm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.Model.Meal
import com.example.hmnm.R

class AllMealsAdapter(
    private var meals: List<Meal>,
    private val onBookmarkClick: (Meal) -> Unit,
    private val onItemClick: (Meal) -> Unit,
    private val onViewClick: (Meal) -> Unit
) : RecyclerView.Adapter<AllMealsAdapter.MealViewHolder>() {

    private val prepTimeMap = mutableMapOf<String, Int>()
    private val ratingMap = mutableMapOf<String, Float>()

    class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.recipe_image)
        val mealName: TextView = itemView.findViewById(R.id.recipe_name)
        val ratingBar: RatingBar = itemView.findViewById(R.id.rating_bar)
        val prepTime: TextView = itemView.findViewById(R.id.preparation_time)
        val bookmarkIcon: ImageView = itemView.findViewById(R.id.bookmark_icon)
        val viewButton: Button = itemView.findViewById(R.id.viewButton)


        fun updateBookmarkIcon(isFavorite: Boolean) {
            bookmarkIcon.setImageResource(
                if (isFavorite) R.drawable.ic_bookmarked_foreground
                else R.drawable.ic_bookmark
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.all_meal_item, parent, false)
        return MealViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]

        Glide.with(holder.itemView.context)
            .load(meal.strMealThumb)
            .placeholder(R.drawable.placholder)
            .into(holder.mealImage)

        holder.mealName.text = meal.strMeal

        val prepTime = prepTimeMap.getOrPut(meal.idMeal) { (10..60).random() }
        holder.prepTime.text = "$prepTime min"

        val rating = ratingMap.getOrPut(meal.idMeal) { (25..50).random() / 10f }
        holder.ratingBar.rating = rating

        holder.updateBookmarkIcon(meal.isFavorite)

        holder.viewButton.setOnClickListener { onViewClick(meal) }

        holder.bookmarkIcon.setOnClickListener {
            if (meal.isFavorite) {
                val context = holder.itemView.context
                val builder = android.app.AlertDialog.Builder(context)

                val title = android.text.SpannableString("Remove favorite")
                title.setSpan(
                    android.text.style.ForegroundColorSpan(
                        androidx.core.content.ContextCompat.getColor(context, R.color.teal_700)
                    ), 0, title.length, 0
                )
                builder.setTitle(title)

                builder.setMessage("Are you sure you want to remove this meal from your favorites?")
                builder.setPositiveButton("Yes") { _, _ ->
                    meal.isFavorite = false
                    holder.updateBookmarkIcon(false)
                    onBookmarkClick(meal)
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
            } else {
                meal.isFavorite = true
                holder.updateBookmarkIcon(true)
                onBookmarkClick(meal)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(meal) }
    }

    override fun getItemCount() = meals.size
}
