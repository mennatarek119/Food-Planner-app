package com.example.hmnm

import FavoritesFragment
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.app.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()


        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        loadFragment(HomeFragment()) 

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> loadFragment(HomeFragment())
                R.id.search -> loadFragment(SearchFragment())
                R.id.calender -> loadFragment(MealPlanFragment())
                R.id.AddToFav -> loadFragment(FavoritesFragment())
                R.id.profile->loadFragment(ProfileFragment())
            }
            true
        }

    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

}
