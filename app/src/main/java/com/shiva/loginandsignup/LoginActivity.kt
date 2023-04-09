package com.shiva.loginandsignup

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.content.Intent
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.developer.gbuttons.GoogleSignInButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView
    private lateinit var forgotPassword: TextView
    private lateinit var googleBtn: GoogleSignInButton
    private lateinit var gOptions: GoogleSignInOptions
    private lateinit var gClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginEmail = findViewById(R.id.login_email)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signUpRedirectText)
        forgotPassword = findViewById(R.id.forgot_password)
        googleBtn = findViewById(R.id.googleBtn)

        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            val email = loginEmail.text.toString()
            val pass = loginPassword.text.toString()

            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (pass.isNotEmpty()) {
                    auth.signInWithEmailAndPassword(email, pass)
                        .addOnSuccessListener {
                            val sharedPreferences = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean(IS_LOGGED_IN_KEY, true)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                            // Start MainActivity or appropriate screen
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    loginPassword.error = "Empty fields are not allowed"
                }
            } else if (email.isEmpty()) {
                loginEmail.error = "Empty fields are not allowed"
            } else {
                loginEmail.error = "Please enter correct email"
            }
        }

        signupRedirectText.setOnClickListener {
            startActivity(Intent(this@LoginActivity, SignUpActivity::class.java))
        }

        forgotPassword.setOnClickListener {
            val builder = AlertDialog.Builder(this@LoginActivity)
            val dialogView = layoutInflater.inflate(R.layout.dialog_forgot, null)
            val emailBox = dialogView.findViewById<EditText>(R.id.emailBox)

            builder.setView(dialogView)
            val dialog = builder.create()

            dialogView.findViewById<View>(R.id.btnReset).setOnClickListener {
                val userEmail = emailBox.text.toString()

                if (TextUtils.isEmpty(userEmail) && !Patterns.EMAIL_ADDRESS.matcher(userEmail)
                        .matches()
                ) {
                    Toast.makeText(this@LoginActivity, "Enter your registered email id", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Reset password using Firebase Auth
                auth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this@LoginActivity, "Password reset email sent", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@LoginActivity, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
                        }
                        dialog.dismiss()
                    }
            }

            dialogView.findViewById<View>(R.id.btnCancel).setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            dialog.show()
        }

        // Set up Google sign-in options
        gOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Create Google sign-in client
        gClient = GoogleSignIn.getClient(this, gOptions)

        // Set up Google sign-in button click listener
        googleBtn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = gClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google sign-in was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google sign-in failed, show error message
                Toast.makeText(this@LoginActivity, "Google sign-in failed", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign-in success, update UI with signed-in user's information
                    Toast.makeText(this@LoginActivity, "Google sign-in successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else {
                    // Sign-in failed, show error message
                    Toast.makeText(
                        this@LoginActivity, "Google sign-in failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val PREFS_KEY = "my_prefs_key" // Replace with your preferred key
        private const val IS_LOGGED_IN_KEY = "is_logged_in" // Replace with your preferred key
    }
}
