package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaymentFragment: Fragment() {

    private lateinit var pod: RadioButton
    private lateinit var pay: RadioButton
    private lateinit var proceed: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var dbRef: DatabaseReference
    private lateinit var orderRef: DatabaseReference
    private lateinit var allOrderRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    private var cart: List<AddCart>? = null
    private var addresses: List<Address>? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_payment,container,false)
        pod = root.findViewById(R.id.radioButton2)
        pay = root.findViewById(R.id.radioButton)
        proceed = root.findViewById(R.id.proceed)
        progressBar = root.findViewById(R.id.pb)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid
        orderRef = FirebaseDatabase.getInstance()
            .getReference("order/$uid/${System.currentTimeMillis()}")

        val d = Calendar.getInstance().time
        val df = SimpleDateFormat("dd MMM yyyy")
        val s = df.format(d)
        allOrderRef = FirebaseDatabase.getInstance()
            .getReference("allOrder/$s")

        cart = ArrayList()
        addresses = ArrayList()

        pod.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                proceed.isEnabled = true
                proceed.text = getString(R.string.place_order)
            }
        }

        pay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                proceed.isEnabled = true
                proceed.text = getString(R.string.proceed)
            }
        }

        proceed.setOnClickListener {
            if (proceed.text == "Place Order"){
                placeOrder()
            } else{
                if (proceed.text == "Proceed"){
                    Toast.makeText(context,"Please Select POD",Toast.LENGTH_SHORT).show()
                }
            }
        }
        return root
    }

    private fun placeOrder() {
        progressBar.visibility = ProgressBar.VISIBLE

        dbRef = FirebaseDatabase.getInstance().getReference("address/$uid/")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (addresses as ArrayList<Address>).clear()
                for (x in p0.children){
                    val i = x.getValue(Address::class.java)
                    if (i?.check!!){
                        orderRef.child("address").setValue(i)
                        return
                    }
                }
            }
        })

        dbRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
                return
            }

            @SuppressLint("SimpleDateFormat")
            override fun onDataChange(p0: DataSnapshot) {

                (cart as ArrayList).clear()

                for (x in p0.children){
                    val description = x.getValue(AddCart::class.java)
                    (cart as ArrayList<AddCart>).add(description!!)
                }

                var q = 0
                var p = 0
                for (i in cart as ArrayList<AddCart>) {
                    q += i.quantity!!.toInt()
                    p += i.price!!.toInt()
                }

                for (i in cart as ArrayList<AddCart>){
                    orderRef.child("products").push().setValue(i)
                }

                orderRef.child("total").child("quantity").setValue(q)
                orderRef.child("total").child("price").setValue(p)

                val d = Calendar.getInstance().time
                val df = SimpleDateFormat("dd MMM yyyy")
                val s = df.format(d)

                orderRef.child("date").child("date").setValue(s)
                orderRef.child("date").child("status").setValue("Order Placed")
                dbRef.removeValue()

                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,"Order placed successfully", Toast.LENGTH_LONG).show()
                val id = orderRef.toString().split("/")

                allOrderRef.push().setValue("order/$uid/${id[id.size-1]}")
                (activity as MainActivity).gotoPod(p)
            }
        })
    }
}