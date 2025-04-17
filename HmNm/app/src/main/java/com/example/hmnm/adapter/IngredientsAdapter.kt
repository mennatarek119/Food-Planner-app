package com.example.hmnm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.hmnm.R
import kotlin.random.Random

class IngredientsAdapter(
    private var items: List<Pair<String, String>>,
    private val onIngredientClick: (String) -> Unit  
) : RecyclerView.Adapter<IngredientsAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ingredients, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ingredientNameTextView: TextView = itemView.findViewById(R.id.ingredientName)
        private val ingredientImageView: ImageView = itemView.findViewById(R.id.ingredientImg)

        fun bind(item: Pair<String, String>) {
            ingredientNameTextView.text = item.first

            Glide.with(ingredientImageView.context)
                .load(item.second)
                .placeholder(R.drawable.placholder)  
                .error(R.drawable.placholder)  
                .into(ingredientImageView)

            itemView.setOnClickListener {
                onIngredientClick(item.first) 
            }
        }
    }
}
