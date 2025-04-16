package com.example.hmnm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var checkBoxRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var auth: FirebaseAuth  // إضافة FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        supportActionBar?.hide()

        // ربط العناصر بملفات XML
        emailInput = findViewById(R.id.emailInput)
        passwordInput = findViewById(R.id.passwordInput)
        checkBoxRemember = findViewById(R.id.checkBoxRemember)
        btnLogin = findViewById(R.id.btnLogin)


        // Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // استرجاع البيانات من SharedPreferences
        val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val savedEmail = sharedPreferences.getString("email", "")
        val savedPassword = sharedPreferences.getString("password", "")
        val rememberMe = sharedPreferences.getBoolean("rememberMe", false)

        // تعبئة الحقول تلقائيًا
        emailInput.setText(savedEmail)
        passwordInput.setText(savedPassword)
        checkBoxRemember.isChecked = rememberMe

        btnLogin.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password, sharedPreferences)
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
        val forgetPasswordTextView = findViewById<TextView>(R.id.forgetPassword)
        forgetPasswordTextView.setOnClickListener {
            showResetPasswordBottomSheet()
        }
    }
    private fun showResetPasswordBottomSheet() {
        val view = layoutInflater.inflate(R.layout.reset_password_bottom_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(view)

        val emailEditText = view.findViewById<EditText>(R.id.resetEmail)
        val resetButton = view.findViewById<Button>(R.id.sendResetButton)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Reset email sent. Check your inbox.", Toast.LENGTH_LONG).show()
                            bottomSheetDialog.dismiss()
                        } else {
                            Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        bottomSheetDialog.show()
    }


    private fun loginUser(email: String, password: String, sharedPreferences: SharedPreferences) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    SharedPrefManager.setLoggedIn(this, true) // حفظ الحالة
                    val userId = auth.currentUser?.uid // جلب ال userId
                    if (userId != null) {
                        // يمكنك استخدام userId هنا لتخزينه في قاعدة البيانات أو استخدامه كما تريد
                        println("User ID: $userId")
                    }

                    val editor = sharedPreferences.edit()

                    if (checkBoxRemember.isChecked) {
                        editor.putString("email", email)
                        editor.putString("password", password)
                        editor.putBoolean("rememberMe", true)
                    } else {
                        editor.remove("email")
                        editor.remove("password")
                        editor.putBoolean("rememberMe", false)
                    }

                    editor.apply()

                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

}
