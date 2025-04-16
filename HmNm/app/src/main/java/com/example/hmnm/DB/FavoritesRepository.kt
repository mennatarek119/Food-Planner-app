package com.example.hmnm.DB

import android.util.Log
import com.example.hmnm.database.AppDatabase
import com.example.hmnm.database.entities.FavoriteMeal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FavoritesRepository(private val db: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun addFavoriteMealToFirestore(favoriteMeal: FavoriteMeal) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .document(favoriteMeal.idMeal)
                .set(favoriteMeal)
                .addOnSuccessListener {
                    Log.d("FavoritesRepository", "تم إضافة الوجبة للمفضلات في Firestore")
                }
                .addOnFailureListener { exception ->
                    Log.e("FavoritesRepository", "فشل إضافة الوجبة للمفضلات في Firestore: $exception")
                }
        }
    }

    suspend fun getFavoritesFromRoom(): List<FavoriteMeal> {
        return withContext(Dispatchers.IO) {
            db.favoriteDao().getFavorites()
        }
    }

    // حذف مفضلة من قاعدة البيانات المحلية
    suspend fun deleteFavoriteFromRoom(favoriteMeal: FavoriteMeal) {
        withContext(Dispatchers.IO) {
            db.favoriteDao().deleteFavoriteById(favoriteMeal.idMeal)
        }
    }

    // جلب المفضلات من Firestore
    suspend fun getFavoritesFromFirestore(): List<FavoriteMeal> {
        val userId = auth.currentUser?.uid
        val favoriteMeals = mutableListOf<FavoriteMeal>()

        if (userId != null) {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .await()

            for (document in snapshot) {
                val favoriteMeal = document.toObject(FavoriteMeal::class.java)
                favoriteMeals.add(favoriteMeal)
            }
        }
        return favoriteMeals
    }
}


