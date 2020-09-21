package com.shah.hindustangraincenter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val toolbar: Toolbar = findViewById(R.id.bar)
        setSupportActionBar(toolbar)

        window.statusBarColor = getColor(R.color.colorPrimaryDark)

        auth = FirebaseAuth.getInstance()

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val frag = LoginFragment()
        fragmentTransaction.add(R.id.container,frag)
        fragmentTransaction.commit()
    }

    internal fun goto(){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val frag = SignupFragment()
        val bs = LoginFragment()
        fragmentTransaction.addToBackStack(bs.toString())
        fragmentTransaction.replace(R.id.container,frag)
        fragmentTransaction.commit()
    }

    internal fun go(s: String){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val frag = PhoneFragment(s)
        val bs = SignupFragment()
        fragmentTransaction.addToBackStack(bs.toString())
        fragmentTransaction.replace(R.id.container,frag)
        fragmentTransaction.commit()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onStart() {
        super.onStart()
    }
}