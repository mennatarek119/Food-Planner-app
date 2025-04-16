package com.example.hmnm

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        val appName = findViewById<TextView>(R.id.app_name)
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        appName.startAnimation(animation)


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            if (SharedPrefManager.isLoggedIn(this)) {
                startActivity(Intent(this, HomeActivity::class.java))
            } else {

                startActivity(Intent(this, WelcomeActivity::class.java))
            }

            finish()
        }, 5000)
    }


}
object SharedPrefManager {
    private const val PREF_NAME = "user_prefs"
    private const val KEY_IS_LOGGED_IN = "isLoggedIn"

    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_LOGGED_IN, isLoggedIn).apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}

