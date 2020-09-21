package com.shah.hindustangraincenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePwd: Fragment(){

    private lateinit var old: TextInputEditText
    private lateinit var new: TextInputEditText
    private lateinit var cnf: TextInputEditText
    private lateinit var br: ProgressBar
    private lateinit var cancel: Button
    private lateinit var save: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.change_pwd_layout,container,false)

        old = root.findViewById(R.id.old_password)
        new = root.findViewById(R.id.new_password)
        cnf = root.findViewById(R.id.confirm_password)
        br = root.findViewById(R.id.loading)
        cancel = root.findViewById(R.id.cancel_button)
        save = root.findViewById(R.id.save_button)

        auth = FirebaseAuth.getInstance()

        save.setOnClickListener {
            br.visibility = ProgressBar.VISIBLE
            when {
                old.text.toString().isEmpty() -> {
                    br.visibility = ProgressBar.GONE
                    old.error = "Enter password"
                    old.requestFocus()
                }
                new.text.toString().isEmpty() -> {
                    br.visibility = ProgressBar.GONE
                    new.error = "Enter password"
                    new.requestFocus()
                }
                cnf.text.toString().isEmpty() -> {
                    br.visibility = ProgressBar.GONE
                    cnf.error = "Enter password"
                    cnf.requestFocus()
                }
                else -> {
                    val em = auth.currentUser?.email.toString()
                    val cred = EmailAuthProvider.getCredential(em, old.text.toString())

                    auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            if (new.text.toString() == cnf.text.toString()) {
                                auth.currentUser?.updatePassword(new.text.toString())
                                    ?.addOnCompleteListener {t ->
                                        if (t.isSuccessful) {
                                            br.visibility = ProgressBar.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Password updated successfully",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onDestroy()
                                        } else {
                                            br.visibility = ProgressBar.GONE
                                            Toast.makeText(
                                                requireContext(),
                                                "Error Updating, try again",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onDestroy()
                                        }
                                    }
                            } else {
                                br.visibility = ProgressBar.GONE
                                cnf.error = "Passwords do not match"
                                cnf.requestFocus()
                            }
                        } else {
                            br.visibility = ProgressBar.GONE
                            old.error = "Incorrect password"
                            old.requestFocus()
                        }
                    }
                }
            }
        }

        cancel.setOnClickListener {
            onDestroy()
        }
        return root
    }
}