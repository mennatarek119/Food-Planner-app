package com.example.hmnm.database

import com.example.hmnm.database.entities.FavoriteMeal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.hmnm.database.dao.FavoriteDao
import com.example.hmnm.database.dao.MealPlanDao
import com.example.hmnm.database.entities.MealPlan

@Database(entities = [FavoriteMeal::class, MealPlan::class], version = 8, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun mealPlanDao(): MealPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ) .addMigrations(MIGRATION_2_3) 
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
        val MIGRATION_2_3 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `meal_plan` (`id` INTEGER PRIMARY KEY NOT NULL, `mealId` TEXT NOT NULL, `mealName` TEXT NOT NULL, `mealThumb` TEXT NOT NULL, `day` TEXT NOT NULL,`isFavorite` INTEGER NOT NULL DEFAULT 0)")

            }
        }

    }
}
