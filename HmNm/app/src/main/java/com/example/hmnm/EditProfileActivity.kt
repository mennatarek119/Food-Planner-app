package com.example.app

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hmnm.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var displayNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        displayNameEditText = findViewById(R.id.display_name)
        emailEditText = findViewById(R.id.email)
        passwordEditText = findViewById(R.id.password)

        loadUserData()

        val saveButton = findViewById<Button>(R.id.save_button)
        saveButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()

            if (newPassword.isNotEmpty() && newPassword.length < 6) {
                Toast.makeText(this, "كلمة المرور يجب أن تكون 6 أحرف على الأقل", Toast.LENGTH_SHORT).show()
            } else {
                saveUserData(newPassword)
            }
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val displayName = document.getString("displayName") ?: ""
                    val email = document.getString("email") ?: ""

                    displayNameEditText.setText(displayName)
                    emailEditText.setText(email)
                } else {
                    Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserData(newPassword: String) {
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedDisplayName = displayNameEditText.text.toString().trim()
        val updatedEmail = emailEditText.text.toString().trim()

        val userData = hashMapOf(
            "displayName" to updatedDisplayName,
            "email" to updatedEmail,
        )

        db.collection("users").document(userId)
            .update(userData as Map<String, Any>)
            .addOnSuccessListener {
                updateFirebaseAuthProfile(updatedDisplayName, updatedEmail, newPassword)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirebaseAuthProfile(displayName: String, email: String, newPassword: String) {
        val user = auth.currentUser

        if (user != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updateEmail(email)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    if (newPassword.isNotEmpty()) {
                                        changeUserPassword(this, newPassword)
                                    } else {
                                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to update email: ${emailTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeUserPassword(context: Context, newPassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        user?.updatePassword(newPassword)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(context, "تم تغيير كلمة المرور بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "حدث خطأ: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            } ?: Toast.makeText(context, "يجب تسجيل الدخول أولاً لتغيير كلمة المرور", Toast.LENGTH_SHORT).show()
    }
}
