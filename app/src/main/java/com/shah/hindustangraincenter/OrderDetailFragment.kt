package com.shah.hindustangraincenter

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderDetailFragment: Fragment(), OrderDetailAdapter.OnSubmitRating{

    private lateinit var orderDetailRecyclerView: RecyclerView
    private lateinit var price: TextView
    private lateinit var quantity: TextView
    private lateinit var name: TextView
    private lateinit var addr1: TextView
    private lateinit var addr2: TextView
    private lateinit var city: TextView
    private lateinit var state: TextView
    private lateinit var pin: TextView
    private lateinit var cancel: Button
    private lateinit var id: TextView
    private lateinit var status: TextView
    private lateinit var date: TextView
    private lateinit var pb: ProgressBar

    private var order: List<AddCart>? = null
    private var keys: List<String>? = null
    private var ratings: List<Float>? = null

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order_detail,container,false)
        orderDetailRecyclerView = root.findViewById(R.id.cart_rv)
        price = root.findViewById(R.id.total_price)
        quantity = root.findViewById(R.id.total_quantity)
        name = root.findViewById(R.id.a_name)
        addr1 = root.findViewById(R.id.a_addr1)
        addr2 = root.findViewById(R.id.a_addr2)
        city = root.findViewById(R.id.a_city)
        state = root.findViewById(R.id.a_state)
        pin = root.findViewById(R.id.a_pin)
        cancel = root.findViewById(R.id.cancel)
        id = root.findViewById(R.id.order_id)
        status = root.findViewById(R.id.order_status)
        date = root.findViewById(R.id.order_date)
        pb = root.findViewById(R.id.pb)

        pb.visibility = ProgressBar.VISIBLE
        val orderId = arguments?.get("orderId").toString()
        id.text = orderId

        order = ArrayList()
        keys = ArrayList()
        ratings = ArrayList()
        orderDetailRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        auth = FirebaseAuth.getInstance()
        uid = auth.uid!!

        databaseRef = FirebaseDatabase.getInstance().getReference("order/$uid/$orderId/date/")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.toString(), Toast.LENGTH_SHORT).show()
                pb.visibility = ProgressBar.GONE
                return
            }

            override fun onDataChange(p0: DataSnapshot) {
                // Toast.makeText(context,p0.child("date").value.toString(),Toast.LENGTH_SHORT).show()
                date.text = p0.child("date").value.toString()

                if (p0.child("status").value.toString() == "Cancelled") {
                    status.setTextColor(ContextCompat.getColor(context!!, R.color.red))
                    cancel.isEnabled = false
                } else {
                    status.setTextColor(ContextCompat.getColor(context!!,R.color.green))
                    cancel.isEnabled = true
                }
                status.text = p0.child("status").value.toString()
            }
        })

        cancel.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Are you sure you want to cancel order?")
                .setPositiveButton("Ok"){dialog, _->
                    databaseRef = FirebaseDatabase.getInstance().getReference("order/$uid/$orderId/date/")
                    databaseRef.child("status").setValue("Cancelled").addOnSuccessListener {
                        dialog.dismiss()
                        cancel.isEnabled = false
                        Toast.makeText(context,"Order cancelled successfully",Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        dialog.dismiss()
                        Toast.makeText(context,"Unable to cancel order, try again later",Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") {dialog, _ ->
                    dialog.dismiss()
                }
                .create().show()
        }

        databaseRef = FirebaseDatabase.getInstance().getReference("order/$uid/$orderId/products/")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                pb.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (order as ArrayList).clear()
                (keys as ArrayList).clear()
                //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()
                for (x in p0.children){
                    val description = x.getValue(AddCart::class.java)
                    (this@OrderDetailFragment.order as ArrayList<AddCart>).add(description!!)
                    (this@OrderDetailFragment.keys as ArrayList<String>).add(x.key.toString())
                }

                showRating(order as ArrayList<AddCart>)
//                val orderDetailAdapter = OrderDetailAdapter(
//                    order as ArrayList<AddCart>,
//                    keys as ArrayList<String>,
//                    this@OrderDetailFragment
//                )
//                orderDetailRecyclerView.adapter = orderDetailAdapter
//
//                var q = 0
//                var p = 0
//                for (i in order as ArrayList<AddCart>) {
//                    q += i.quantity!!.toInt()
//                    p += i.price!!.toInt()
//                }
//                quantity.text = q.toString()
//                price.text = getString(R.string.pricing,p.toString())
                // progressBar.visibility = ProgressBar.GONE
            }
        })

        databaseRef = FirebaseDatabase.getInstance().getReference("order/$uid/$orderId/address/")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                pb.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val i = p0.getValue(Address::class.java)
                showAdd(i!!)
                pb.visibility = ProgressBar.GONE
            }
        })

        return root
    }

    private fun showRating(
        order: ArrayList<AddCart>
    ) {

        var counter = 0
        for (o in order) {
            val getKey = o.costRef?.split("/")
            val key = getKey?.get(1)
            Log.d("rating", "success")
            databaseRef = FirebaseDatabase.getInstance().getReference("rating/$key/$uid")

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(context, p0.message, Toast.LENGTH_SHORT).show()
                    return
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val rate = p0.value
                    if (rate == null) {
                        (ratings as ArrayList<Float>).add(0.0F)
                    } else {
                        (ratings as ArrayList<Float>).add(rate.toString().toFloat())
                    }
                    counter++
                    if (counter >= order.size) {
                        val orderDetailAdapter = OrderDetailAdapter(
                            order,
                            ratings as ArrayList<Float>,
                            this@OrderDetailFragment
                        )
                        orderDetailRecyclerView.adapter = orderDetailAdapter

                        var q = 0
                        var p = 0
                        for (i in this@OrderDetailFragment.order as ArrayList<AddCart>) {
                            q += i.quantity!!.toInt()
                            p += i.price!!.toInt()
                        }
                        quantity.text = q.toString()
                        price.text = getString(R.string.pricing,p.toString())
                    }
                }

            })
        }
    }

    private fun showAdd(x: Address) {
        name.text = x.name
        addr1.text = x.addr1
        addr2.text = x.addr2
        city.text = x.city
        state.text = x.state
        pin.text = x.pin
    }

    override fun submit(cart: AddCart, rating: Float) {
        val getKey = cart.costRef?.split("/")
        val key = getKey?.get(1)
        if (status.text.toString() == "Delivered") {
            val ratingDB  = FirebaseDatabase.getInstance().getReference("rating/$key/")
            ratingDB.child(uid).setValue(rating)
            Toast.makeText(context,"Rating submitted",Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context,"Product not yet delivered. You can rate the products once they are Delivered. ",Toast.LENGTH_LONG).show()
        }
    }
}