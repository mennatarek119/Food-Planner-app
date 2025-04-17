package com.example.hmnm.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.R
import com.example.hmnm.database.entities.MealPlan
import com.example.hmnm.ui.AllMealsActivity

class DaysAdapter(
    private val daysList: List<String>, 
    private val mealsListMap: Map<String, List<MealPlan>>, 
    private val context: Context
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.dayTextView)
        val addButton: Button = itemView.findViewById(R.id.addButton)
        val mealsRecyclerView: RecyclerView = itemView.findViewById(R.id.rvMealsDays)

        fun bind(day: String, mealsList: List<MealPlan>, context: Context) {
            dayTextView.text = day
            addButton.setOnClickListener {
                val intent = Intent(context, AllMealsActivity::class.java)
                intent.putExtra("selected_day", day)
                context.startActivity(intent)
            }

            val mealsAdapter = MealsDaysAdapter(
                mealsList.toMutableList(), 
                onRemoveClick = { meal ->  },
            )
            mealsRecyclerView.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = mealsAdapter
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_item, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = daysList[position]
        val mealsList = mealsListMap[day] ?: emptyList() 
        holder.bind(day, mealsList, context)
    }

    override fun getItemCount(): Int = daysList.size
}
