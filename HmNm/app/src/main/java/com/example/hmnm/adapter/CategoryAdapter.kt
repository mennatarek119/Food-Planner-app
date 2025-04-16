package com.example.hmnm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.Model.Category
import com.example.hmnm.R

class CategoryAdapter(
    private var categories: List<Category>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateItems(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val categoryNameTextView: TextView = itemView.findViewById(R.id.categoryName)
        private val categoryImageView: ImageView = itemView.findViewById(R.id.categoryImg)

        fun bind(category: Category) {
            categoryNameTextView.text = category.strCategory
            Glide.with(categoryImageView.context)
                .load(category.strCategoryThumb)
                .into(categoryImageView)

            // عند الضغط على العنصر، يتم إرسال اسم الفئة إلى `onCategoryClick`
            itemView.setOnClickListener {
                onCategoryClick(category.strCategory)
            }
        }
    }
}
