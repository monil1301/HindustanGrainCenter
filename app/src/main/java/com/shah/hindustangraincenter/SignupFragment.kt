package com.shah.hindustangraincenter

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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase

class SignupFragment() : Fragment(), TextView.OnEditorActionListener {

    private lateinit var name: TextInputEditText
    private lateinit var email: TextInputEditText

    private lateinit var pass: TextInputEditText
    private lateinit var cnPass: TextInputEditText

    private lateinit var signUp: Button
    private lateinit var to: TextView
    private lateinit var pb: ProgressBar
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_signup, container, false)
        name = root.findViewById(R.id.sp_name)
        email = root.findViewById(R.id.sp_email)

        pass = root.findViewById(R.id.sp_password)
        cnPass = root.findViewById(R.id.cn_password)
        signUp = root.findViewById(R.id.signup)
        to = root.findViewById(R.id.to_login)
        pb = root.findViewById(R.id.loading)
        auth = FirebaseAuth.getInstance()

        cnPass.setOnEditorActionListener(this)

        to.setOnClickListener {
            to()
        }

        signUp.setOnClickListener {
            signUP()
        }


        return root
    }

    private fun signUP(){
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        when {
            name.text.toString().isEmpty() -> {
                name.error = "Enter Name"
                name.requestFocus()
            }
            email.text.toString().isEmpty() -> {
                email.error = "Enter email id"
                email.requestFocus()
            }
            pass.text.toString().isEmpty() -> {
                pass.error = "Enter password"
                pass.requestFocus()
            }
            cnPass.text.toString().isEmpty() -> {
                cnPass.error = "Enter password"
                cnPass.requestFocus()
            }
            pass.text.toString().trim() != cnPass.text.toString().trim() -> {
                cnPass.error = "Password do not match"
                cnPass.requestFocus()
            }
            else -> {
                pb.visibility = ProgressBar.VISIBLE
                auth.createUserWithEmailAndPassword(email.text.toString(), pass.text.toString())
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val setDetails = UserProfileChangeRequest.Builder()
                                .setDisplayName(name.text.toString())
                                .build()


                            auth.currentUser?.updateProfile(setDetails)?.addOnCompleteListener {t ->
                                if (t.isSuccessful) {
                                    Toast.makeText(
                                        context,
                                        "Account created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    (activity as LoginActivity).go(pass.text.toString().trim())
//                                            val i = Intent(context, MainActivity::class.java)
//                                            startActivity(i)
                                    pb.visibility = ProgressBar.GONE
                                }
                            }
                        }
                    }.addOnFailureListener{
                        Toast.makeText(
                            context,
                            it.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        pb.visibility = ProgressBar.GONE
                    }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun to(){
        val fm = fragmentManager
        fm?.popBackStack()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO){
            signUP()
        }
        return true
    }
}