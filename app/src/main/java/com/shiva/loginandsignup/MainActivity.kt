package com.shiva.loginandsignup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_KEY = "my_prefs_key" // Replace with your preferred key
        private const val IS_LOGGED_IN_KEY = "is_logged_in" // Replace with your preferred key
        private const val LOGIN_REQUEST_CODE = 1001 // Replace with your preferred request code
    }

    private lateinit var userName: TextView
    private lateinit var logout: Button
    private lateinit var gClient: GoogleSignInClient
    private lateinit var gOptions: GoogleSignInOptions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        logout = findViewById(R.id.logout)
        userName = findViewById(R.id.userName)

        gOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        gClient = GoogleSignIn.getClient(this, gOptions)

        val gAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (gAccount != null) {
            val gName = gAccount.displayName
            userName.text = gName
        }

        logout.setOnClickListener {
            // Sign out the user from Google Sign-In
            gClient.signOut().addOnCompleteListener(this, OnCompleteListener {
                // Clear the logged-in status in shared preferences
                val prefs = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
                val editor = prefs.edit()
                editor.putBoolean(IS_LOGGED_IN_KEY, false)
                editor.apply()

                // Finish the current activity and start LoginActivity
                finish()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            })
        }
    }
}
