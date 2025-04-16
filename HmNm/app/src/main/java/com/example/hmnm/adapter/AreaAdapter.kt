package com.example.hmnm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hmnm.R

class AreaAdapter(
    private var items: List<Pair<String, Int>>,
    private val onItemClick: (String) -> Unit // إضافة دالة الــ click هنا
) : RecyclerView.Adapter<AreaAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_country, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Pair<String, Int>>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val areaNameTextView: TextView = itemView.findViewById(R.id.areaName)
        private val flagImageView: ImageView = itemView.findViewById(R.id.areaImg)

        fun bind(item: Pair<String, Int>) {
            areaNameTextView.text = item.first
            flagImageView.setImageResource(item.second)  // عرض صورة العلم

            // إضافة مستمع للـ click
            itemView.setOnClickListener {
                onItemClick(item.first) // إرسال اسم المنطقة عند الضغط
            }
        }
    }
}

