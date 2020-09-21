package com.shah.hindustangraincenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderReviewFragment: Fragment() {

    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var price: TextView
    private lateinit var quantity: TextView
    private lateinit var name: TextView
    private lateinit var addr1: TextView
    private lateinit var addr2: TextView
    private lateinit var city: TextView
    private lateinit var state: TextView
    private lateinit var pin: TextView
    private lateinit var payment: Button

    private var order: List<AddCart>? = null
    private var addresses: List<Address>? = null

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order_review,container,false)
        orderRecyclerView = root.findViewById(R.id.cart_rv)
        price = root.findViewById(R.id.total_price)
        quantity = root.findViewById(R.id.total_quantity)
        name = root.findViewById(R.id.a_name)
        addr1 = root.findViewById(R.id.a_addr1)
        addr2 = root.findViewById(R.id.a_addr2)
        city = root.findViewById(R.id.a_city)
        state = root.findViewById(R.id.a_state)
        pin = root.findViewById(R.id.a_pin)
        payment = root.findViewById(R.id.payment)

        order = ArrayList()
        orderRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        auth = FirebaseAuth.getInstance()
        uid = auth.uid!!


        addresses = ArrayList()

        databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (order as ArrayList).clear()
                //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()
                for (x in p0.children){
                    val description = x.getValue(AddCart::class.java)
                    (order as ArrayList<AddCart>).add(description!!)
                }

                val orderReviewAdapter = OrderReviewAdapter(
                    order as ArrayList<AddCart>
                )
                orderRecyclerView.adapter = orderReviewAdapter

                var q = 0
                var p = 0
                for (i in order as ArrayList<AddCart>) {
                    q += i.quantity!!.toInt()
                    p += i.price!!.toInt()
                }
                quantity.text = q.toString()
                price.text = getString(R.string.pricing,p.toString())
               // progressBar.visibility = ProgressBar.GONE
            }
        })

        databaseRef = FirebaseDatabase.getInstance().getReference("address/$uid/")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (addresses as ArrayList<Address>).clear()
                for (x in p0.children){
                    val i = x.getValue(Address::class.java)
                    if (i?.check!!){
                        showAddress(i)
                        return
                    }
                }
            }
        })

        payment.setOnClickListener {
            (activity as MainActivity).goto(6)
        }

        return root
    }

    private fun showAddress(x: Address) {
        name.text = x.name
        addr1.text = x.addr1
        addr2.text = x.addr2
        city.text = x.city
        state.text = x.state
        pin.text = x.pin
    }
}