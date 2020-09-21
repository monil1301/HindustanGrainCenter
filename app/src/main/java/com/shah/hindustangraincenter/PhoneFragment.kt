package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.concurrent.TimeUnit

class PhoneFragment(private val pass: String): Fragment(), TextView.OnEditorActionListener {

    private lateinit var phone: TextInputEditText
    private lateinit var otp: TextInputEditText
    private lateinit var getOtp: TextView
    private lateinit var resendOtp: TextView
    private lateinit var subOtp: Button
    private lateinit var pb: ProgressBar
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private lateinit var email: String
    private lateinit var code: String
    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_phone,container,false)
        phone = root.findViewById(R.id.sp_phone)
        otp = root.findViewById(R.id.sp_otp)
        getOtp = root.findViewById(R.id.get_otp)
        resendOtp = root.findViewById(R.id.resend_otp)
        subOtp = root.findViewById(R.id.cont)
        pb = root.findViewById(R.id.pb)
        auth = FirebaseAuth.getInstance()

        uid = auth.currentUser?.uid!!
        email = auth.currentUser?.email!!

        dbRef = FirebaseDatabase.getInstance().getReference("phones/$uid/")

        otp.setOnEditorActionListener(this)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                code = p0
            }

            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                //pb.visibility = ProgressBar.GONE
                val c = p0.smsCode
                if (c != null) {
                    otp.setText(c)
                    //pb.visibility = ProgressBar.VISIBLE
                    verify(c)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(
                    context,
                    p0.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                pb.visibility = ProgressBar.GONE
                getOtp.isEnabled = true
            }
        }

        getOtp.setOnClickListener {
            getOtp.isEnabled = false
            val view = activity?.currentFocus
            if (view != null) {
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            pb.visibility = ProgressBar.VISIBLE
            val phoneNumber = "+91" + phone.text.toString().trim()
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60,
                TimeUnit.SECONDS,
                LoginActivity(),
                callbacks
            )
            resendTimer()
        }

        subOtp.setOnClickListener {
           subOTP()
        }

        return root
    }

    private fun resendTimer() {
        resendOtp.visibility = TextView.VISIBLE
        val timer = object: CountDownTimer(30000,1000){
            override fun onFinish() {
                getOtp.isEnabled = true
                getOtp.text = getString(R.string.resend_otp)
                resendOtp.visibility = TextView.GONE
                pb.visibility = ProgressBar.GONE
            }

            override fun onTick(millisUntilFinished: Long) {
                val t = millisUntilFinished/1000
                resendOtp.text = getString(R.string.resend_otp_in, t.toInt())
            }
        }
        timer.start()
    }

    private fun subOTP() {
        val view = activity?.currentFocus
        if (view != null) {
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        pb.visibility = ProgressBar.VISIBLE
        val c = otp.text.toString().trim()
        if (c.isNotEmpty()) {
            verify(c)
        } else{
            otp.error = "Otp cannot be empty"
            otp.requestFocus()
        }
    }

    private fun verify(c: String?) {
        if (c != null) {
            val v = PhoneAuthProvider.getCredential(code, c)
            auth.signInWithCredential(v)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            context,
                            "Phone number verified successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        auth.signOut()
                        dbRef.child("phone").setValue(phone.text.toString().trim())
                        auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener {t ->
                            if (t.isSuccessful){
                               // Toast.makeText(context,"Logged In successfully",Toast.LENGTH_SHORT).show()
                                val i = Intent(context,MainActivity::class.java)
                                startActivity(i)
                                pb.visibility = ProgressBar.GONE
                            } else {
                                pb.visibility = ProgressBar.GONE
                                Toast.makeText(context,t.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        Toast.makeText(
                            context,
                            "Verification failed",
                            Toast.LENGTH_SHORT
                        ).show()
                        getOtp.isEnabled = true
                    }
                }
        }
        else{
            otp.error = "Cannot be null"
            otp.requestFocus()
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_GO){
            subOTP()
        }
        return true
    }
}