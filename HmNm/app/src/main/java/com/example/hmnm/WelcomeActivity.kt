package com.example.hmnm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hmnm.databinding.ActivityWelcomeBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        // Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Google Sign-In Configuration
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()

        binding.googleLoginButton.setOnClickListener {
            signInWithGoogle()
        }

        // Facebook Sign-In Configuration
        callbackManager = CallbackManager.Factory.create()

        binding.facebookLoginButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken)
                }

                override fun onCancel() {
                    Toast.makeText(this@WelcomeActivity, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: FacebookException) {
                    Toast.makeText(this@WelcomeActivity, "Facebook Login Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // Google Sign-In Function
    private fun signInWithGoogle() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                SharedPrefManager.setLoggedIn(this, true) // حفظ الحالة
                val signInIntent = result.pendingIntent.intentSender
                startIntentSenderForResult(
                    signInIntent,
                    1001,
                    null, 0, 0, 0, null
                )
            }
            .addOnFailureListener(this) {
                Toast.makeText(this, "Google Sign-In Failed", Toast.LENGTH_SHORT).show()
            }
    }

    // Facebook Token Handling
    private fun handleFacebookAccessToken(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    SharedPrefManager.setLoggedIn(this, true) // حفظ الحالة
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Facebook Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Handling Activity Results
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Facebook Login
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Google Login
        if (requestCode == 1001) {
            val credential = oneTapClient.getSignInCredentialFromIntent(data)
            val idToken = credential?.googleIdToken

            if (idToken != null) {
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Firebase Auth Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    // Navigation Functions
    fun goToHomeActivity(view: View) {
        FirebaseAuth.getInstance().signOut()
        SharedPrefManager.clearPrefs(this)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun goToSignUp(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    fun goTologin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
