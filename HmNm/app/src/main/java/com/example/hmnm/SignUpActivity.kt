package com.example.hmnm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val displayNameEditText = findViewById<TextInputEditText>(R.id.etDisplayName)
        val emailEditText = findViewById<TextInputEditText>(R.id.etEmail)
        val passwordEditText = findViewById<TextInputEditText>(R.id.etPassword)
        val confirmPasswordEditText = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val signUpButton = findViewById<Button>(R.id.btnNext)

        signUpButton.setOnClickListener {
            val displayName = displayNameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            val confirmPassword = confirmPasswordEditText.text.toString().trim()

            if (displayName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signUpUser(displayName, email, password)
        }
    }

    private fun signUpUser(displayName: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            storeUserDataInFirestore(user)

                            Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Sign up failed"
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun storeUserDataInFirestore(user: FirebaseUser?) {
        user?.let {
            val userId = it.uid
            val displayName = it.displayName
            val email = it.email

            val userData = hashMapOf(
                "userId" to userId,
                "displayName" to displayName,
                "email" to email,
            )

            db.collection("users")
                .document(userId)  
                .set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "User data stored successfully")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error storing user data", e)
                }
        }
    }

}
