package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth


class LoginFragment: Fragment(), TextView.OnEditorActionListener {

    private lateinit var email: EditText
    private lateinit var pass: EditText
    private lateinit var logIn: Button
    private lateinit var to: TextView
    private lateinit var pb: ProgressBar
    private lateinit var forget: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_login,container,false)
        email = root.findViewById(R.id.email)
        pass = root.findViewById(R.id.password)
        logIn = root.findViewById(R.id.login)
        to = root.findViewById(R.id.to_signup)
        pb = root.findViewById(R.id.loading)
        forget = root.findViewById(R.id.forget)
        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = auth.currentUser
            if (user != null) {
                Toast.makeText(context, "Logged In successfully", Toast.LENGTH_SHORT).show()
                val i = Intent(context, MainActivity::class.java)
                startActivity(i)
            }
        }

        to.setOnClickListener {
            (activity as LoginActivity).goto()
        }

        pass.setOnEditorActionListener(this)

        logIn.setOnClickListener {
            logIN()
        }

        forget.setOnClickListener {
            val l = LayoutInflater.from(context).inflate(R.layout.forgot_pwd_layout,null)
            val em = l.findViewById<TextInputEditText>(R.id.forgot)
            AlertDialog.Builder(context)
                .setTitle("Forgot Password?")
                .setView(l)
                .setPositiveButton("Ok"){dialog, _ ->
                    pb.visibility = ProgressBar.VISIBLE
                    val email = em.text.toString().trim()
                    if (em.text.toString().isNotEmpty()){
                        auth.sendPasswordResetEmail(email).addOnSuccessListener {
                            Toast.makeText(context,"Password reset link successfully sent on your email",
                                Toast.LENGTH_LONG).show()
                            pb.visibility = ProgressBar.GONE
                        }.addOnFailureListener {
                            Toast.makeText(context,it.toString(),
                                Toast.LENGTH_SHORT).show()
                            pb.visibility = ProgressBar.GONE
                        }
                        dialog.dismiss()
                    } else{
                        em.error = "cannot be empty"
                        em.requestFocus()
                        pb.visibility = ProgressBar.GONE
                    }
                }
                .setNegativeButton("Cancel"){dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }
        return root
    }

    private fun logIN() {
        val view = activity?.currentFocus
        if (view != null){
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        when {
            email.text.isEmpty() -> {
                email.error = "Enter email id"
                email.requestFocus()
            }
            pass.text.isEmpty() -> {
                pass.error = "Enter password"
                pass.requestFocus()
            }
            else -> {
                pb.visibility = ProgressBar.VISIBLE
                auth.signInWithEmailAndPassword(email.text.toString(),pass.text.toString()).addOnCompleteListener {
                    if (it.isSuccessful){
                        Toast.makeText(context,"Logged In successfully",Toast.LENGTH_SHORT).show()
                        val i = Intent(context,MainActivity::class.java)
                        startActivity(i)
                        pb.visibility = ProgressBar.GONE
                    }
                }.addOnFailureListener {
                    pb.visibility = ProgressBar.GONE
                    Toast.makeText(context,it.message,Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
//        auth.addAuthStateListener(authStateListener)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO){
            logIN()
        }
        return true
    }
}