package com.shah.hindustangraincenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = auth.currentUser
            if (user != null) {
                Toast.makeText(this, "Logged In successfully", Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1500)
            } else {
                Handler().postDelayed({
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                    finish()
                }, 1500)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //auth.addAuthStateListener(authStateListener)
    }
}
