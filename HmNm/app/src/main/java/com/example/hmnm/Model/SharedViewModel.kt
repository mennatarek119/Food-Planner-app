package com.example.hmnm.Model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.hmnm.Model.Meal

interface OnMealAddedListener {
    fun onMealAdded(meal: Meal)
}


