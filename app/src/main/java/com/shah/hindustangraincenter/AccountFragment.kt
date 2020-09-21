package com.shah.hindustangraincenter

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.concurrent.TimeUnit

class AccountFragment: Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var name: TextInputEditText
    private lateinit var email: TextInputEditText
    private lateinit var phone: TextInputEditText
    private lateinit var submit: TextView
    private lateinit var change: TextView
    private lateinit var chng: TextView
    private lateinit var pb: ProgressBar
    private lateinit var chgPwd: Button
    private lateinit var chgImg: Button
    private lateinit var deactivate: Button
    private lateinit var manage: Button
    private lateinit var profile: ImageView
    private lateinit var code: String

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_account,container,false)
        name = root.findViewById(R.id.user_name)
        email = root.findViewById(R.id.user_email)
        phone = root.findViewById(R.id.user_phone)
        submit = root.findViewById(R.id.user_submit)
        change = root.findViewById(R.id.user_chng)
        pb = root.findViewById(R.id.loading)
        chgPwd = root.findViewById(R.id.user_chg_pwd)
        chgImg = root.findViewById(R.id.user_img_ch)
        chng = root.findViewById(R.id.user_phone_chng)
        deactivate = root.findViewById(R.id.user_deactivate)
        manage = root.findViewById(R.id.user_addresses)
        profile = root.findViewById(R.id.user_img)

        pb.visibility = ProgressBar.VISIBLE
        auth = FirebaseAuth.getInstance()
        val nm= auth.currentUser?.displayName.toString()
        val em = auth.currentUser?.email.toString()
        val im = auth.currentUser?.photoUrl
        val uid = auth.currentUser?.uid
        setImg(im)

        databaseRef = FirebaseDatabase.getInstance().getReference("phones")

        name.setText(nm)
        email.setText(em)

        databaseRef.child(uid!!).child("phone")
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                pb.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(),Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                pb.visibility = ProgressBar.GONE
                phone.setText(p0.value.toString())
            }
        })

        submit.setOnClickListener {
            pb.visibility = ProgressBar.VISIBLE
            if (name.text.toString().isEmpty()){
                pb.visibility = ProgressBar.GONE
                name.error = "Enter name"
                name.requestFocus()
            } else {
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(name.text.toString())
                    .build()

                auth.currentUser?.updateProfile(profileChangeRequest)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Error Updating", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        change.setOnClickListener {
            pb.visibility = ProgressBar.VISIBLE
            if (email.text.toString().isEmpty()){
                pb.visibility = ProgressBar.GONE
                email.error = "Enter email"
                email.requestFocus()
            } else {
                auth.currentUser?.updateEmail(email.text.toString())?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Error Updating", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        chng.setOnClickListener {
            val layout = LayoutInflater.from(context).inflate(R.layout.deactivate_layout,null)
            val editText= layout.findViewById<TextInputEditText>(R.id.del)
            AlertDialog.Builder(requireContext())
                .setView(layout)
                .setTitle("Change phone number")
                .setPositiveButton("Confirm"){dialog, _ ->
                    pb.visibility = ProgressBar.VISIBLE
                    val cred = EmailAuthProvider.getCredential(em, editText.text.toString())

                    auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                        if (it.isSuccessful){
                            dialog.dismiss()
                            val l = LayoutInflater.from(context).inflate(R.layout.phone_change_layout,null)
                            val phone= l.findViewById<TextInputEditText>(R.id.ch_phone)
                            val otp= l.findViewById<TextInputEditText>(R.id.ch_otp)
                            val get= l.findViewById<TextView>(R.id.get_otp)
                            val bar = l.findViewById<ProgressBar>(R.id.loading)
                           val alert = AlertDialog.Builder(requireContext())
                                .setView(l)
                                .setTitle("Change phone number")
                                .setPositiveButton("Ok"){d, _ ->
                                    verify(otp.text.toString().trim(),phone.text.toString().trim(),otp,d)
                                }
                                .setNegativeButton("Cancel"){d, _ ->
                                    d.dismiss()
                                    pb.visibility = ProgressBar.GONE
                                }
                                val b = alert.show()
                            get.setOnClickListener {
                                bar.visibility = ProgressBar.VISIBLE
                                otpDetect(phone.text.toString().trim(),otp,b,bar)
                            }
                        }
                        else {
                            pb.visibility = ProgressBar.GONE
                            Toast.makeText(context,"Incorrect password",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel"){dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

        chgPwd.setOnClickListener{
            (activity as MainActivity).goto(2)
        }

        manage.setOnClickListener {
            (activity as MainActivity).goto(3)
        }

        deactivate.setOnClickListener {
            val layout = LayoutInflater.from(context).inflate(R.layout.deactivate_layout,null)
            val editText= layout.findViewById<TextInputEditText>(R.id.del)
            AlertDialog.Builder(requireContext())
                .setView(layout)
                .setTitle("Deactivate Account")
                .setPositiveButton("Confirm"){dialog, _ ->
                    pb.visibility = ProgressBar.VISIBLE
                    val cred = EmailAuthProvider.getCredential(em, editText.text.toString())

                    auth.currentUser?.reauthenticate(cred)?.addOnCompleteListener {
                        if (it.isSuccessful){
                            auth.currentUser?.delete()?.addOnCompleteListener {t ->
                                if (t.isSuccessful){
                                    pb.visibility = ProgressBar.GONE
                                    Toast.makeText(context,"Account deleted successfully",Toast.LENGTH_SHORT).show()
                                    val i = Intent(context,LoginActivity::class.java)
                                    startActivity(i)
                                }
                                else {
                                    pb.visibility = ProgressBar.GONE
                                    dialog.dismiss()
                                    Toast.makeText(context,"Error deleting, try again later",Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        else {
                            pb.visibility = ProgressBar.GONE
                            Toast.makeText(context,"Incorrect password",Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel"){dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

        chgImg.setOnClickListener {
            val layout = LayoutInflater.from(context).inflate(R.layout.change_img_layout, null)
            val gallery = layout.findViewById<Button>(R.id.button)
            val take = layout.findViewById<Button>(R.id.button2)
            val builder = AlertDialog.Builder(requireContext())
                .setView(layout)
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
            val alert = builder.show()

            gallery.setOnClickListener {
                alert.dismiss()
                checkPermission(0)
            }

            take.setOnClickListener {
                alert.dismiss()
                checkPermission(1)
            }
        }
    return root
    }

    private fun otpDetect(
        p: String,
        otp: TextInputEditText,
        d: AlertDialog,
        bar: ProgressBar
    ) {
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
                    verify(c,p,otp,d)
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                Toast.makeText(
                    context,
                    p0.message.toString(),
                    Toast.LENGTH_SHORT
                ).show()
                bar.visibility = ProgressBar.GONE
            }
        }

        val phoneNumber = "+91$p"
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            60,
            TimeUnit.SECONDS,
            MainActivity(),
            callbacks
        )
    }

    private fun verify(c: String?,p: String,otp: TextInputEditText,d: Any) {
        if (c != null) {
            val v = PhoneAuthProvider.getCredential(code, c).smsCode
            val uid = auth.currentUser?.uid
            val x = d as DialogInterface

            if (v == c){
                databaseRef.child(uid!!).child("phone").setValue(p)
                Toast.makeText(
                    context,
                    "Phone number verified and added successfully",
                    Toast.LENGTH_SHORT
                ).show()
                x.dismiss()
            } else{
                Toast.makeText(
                    context,
                    "Verification failed",
                    Toast.LENGTH_SHORT
                ).show()
                x.dismiss()
            }
        } else{
            otp.error = "Cannot be null"
            otp.requestFocus()
        }
    }

    private fun setImg(im: Uri?) {
        Picasso.get()
            .load(im)
            .fit()
            .into(profile)
    }

    private fun reqPermission(i: Int){
        if (i == 0) {
           requestPermissions(
               arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),0)
        }
        if (i == 1){
            requestPermissions(
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA),1)
        }
    }

    private fun checkPermission(i: Int){
        if (i == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                ) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            (activity as MainActivity),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    ) {
                        val builder = AlertDialog.Builder(requireContext())
                        val tv = TextView(context)
                        tv.text = getString(R.string.photo_permission)
                        builder.setTitle("Give permission")
                        builder.setView(tv)
                        builder.setNeutralButton("OK") { dialog, _ ->
                            reqPermission(0)
                            dialog.dismiss()
                        }
                        builder.create().show()
                    } else {
                        reqPermission(0)
                    }
                } else {
                    gallery()
                }
            }
        }
        if (i == 1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    requireContext().checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            (activity as MainActivity),
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) || ActivityCompat.shouldShowRequestPermissionRationale(
                            (activity as MainActivity),
                            Manifest.permission.CAMERA
                        )
                    ) {
                        val builder = AlertDialog.Builder(requireContext())
                        val tv = TextView(context)
                        tv.text = getString(R.string.camera_permission)
                        builder.setTitle("Give permission")
                        builder.setView(tv)
                        builder.setNeutralButton("OK") { dialog, _ ->
                            reqPermission(1)
                            dialog.dismiss()
                        }
                        builder.create().show()
                    } else {
                        reqPermission(1)
                    }
                } else{
                    takePhoto()
                }
                }
            }
        }

    private fun takePhoto() {
        val take = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        take.putExtra("android.intent.extras.CAMERA.FACING",1)
        startActivityForResult(take,13)
    }

    private fun gallery() {
        val pick = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      //  pick.putExtra("crop","true")
        pick.putExtra("return-data",true)
        pick.putExtra("aspectX",1)
        pick.putExtra("aspectY",1)
//        pick.putExtra("outputX",100)
//        pick.putExtra("outputY",100)
        startActivityForResult(pick, 0)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    checkPermission(0)
                else
                    gallery()
            }
            1 -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED)
                    checkPermission(1)
                else
                    takePhoto()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED){
            if (requestCode == 0 && data != null){
                val img = data.data
                pb.visibility = ProgressBar.VISIBLE
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setPhotoUri(img)
                    .build()

                auth.currentUser?.updateProfile(profileChangeRequest)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        pb.visibility = ProgressBar.GONE
                        setImg(img)
                        Toast.makeText(context, "Uploaded successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Error Uploading", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
            if (requestCode == 13 && data != null){
                val img = data.data
                pb.visibility = ProgressBar.VISIBLE
                val profileChangeRequest = UserProfileChangeRequest.Builder()
                    .setPhotoUri(img)
                    .build()

                auth.currentUser?.updateProfile(profileChangeRequest)?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        pb.visibility = ProgressBar.GONE
                        profile.setImageURI(img)
                        Toast.makeText(context, "Uploaded successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        pb.visibility = ProgressBar.GONE
                        Toast.makeText(context, "Error Uploading", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }
}