package com.shah.hindustangraincenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class OrderFragment: Fragment(), OrderAdapter.Onclick {

    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var pb: ProgressBar

    private lateinit var dbRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    private var orderId: List<String>? = null
    private var products: List<AddCart>? = null
    private var titles: List<String>? = null
    private var imgs: List<String>? = null
    private var dates: List<String>? = null
    private var status: List<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order,container,false)
        orderRecyclerView = root.findViewById(R.id.order_rv)
        pb = root.findViewById(R.id.pb)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid

        orderId = ArrayList()

        dbRef = FirebaseDatabase.getInstance().getReference("order/$uid/")

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context,p0.toString(),Toast.LENGTH_SHORT).show()
                pb.visibility = ProgressBar.GONE
                return
            }

            override fun onDataChange(p0: DataSnapshot) {
                (orderId as ArrayList<String>).clear()
                for (i in p0.children){
                    (orderId as ArrayList<String>).add(i.key!!)
                }
                orderDiff(orderId as ArrayList<String>)
               // Toast.makeText(context,orderId.toString(),Toast.LENGTH_SHORT).show()
            }

        })
        return root
    }

    private fun orderDiff(o: ArrayList<String>) {
        products = ArrayList()
        titles = ArrayList()
        imgs = ArrayList()
        var count = 0
        (titles as ArrayList<String>).clear()
        (imgs as ArrayList<String>).clear()
        for (i in o){
            dbRef = FirebaseDatabase.getInstance().getReference("order/$uid/$i/products/")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(context,p0.toString(),Toast.LENGTH_SHORT).show()
                    pb.visibility = ProgressBar.GONE
                    return
                }

                override fun onDataChange(p0: DataSnapshot) {
                    (products as ArrayList<AddCart>).clear()
                    var q = -1
                    for (x in p0.children){
                        (products as ArrayList<AddCart>).add(x.getValue(AddCart::class.java)!!)
                        q += x.getValue(AddCart::class.java)!!.quantity!!.toInt()
                    }
                    val t = (products as ArrayList<AddCart>)[0].name + " + " + q.toString() + " others"
                    val im = (products as ArrayList<AddCart>)[0].imageUrl

                    (titles as ArrayList<String>).add(t)
                    (imgs as ArrayList<String>).add(im!!)
                    count ++
                    if (count == o.size){
                        showItems(o,(titles as ArrayList<String>), imgs as ArrayList<String>)
                    }
                }
            })
        }
    }

    private fun showItems(
        o: ArrayList<String>,
        titles: ArrayList<String>,
        imgs: ArrayList<String>
    ) {
        dates = ArrayList()
        status = ArrayList()
        (dates as ArrayList<String>).clear()
        (status as ArrayList<String>).clear()
        orderRecyclerView.layoutManager = LinearLayoutManager(context)
        var count = 0
        for (i in o){
            dbRef = FirebaseDatabase.getInstance().getReference("order/$uid/$i/date/")
            dbRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                   Toast.makeText(context,p0.toString(),Toast.LENGTH_SHORT).show()
                   pb.visibility = ProgressBar.GONE
                   return
                }

                override fun onDataChange(p0: DataSnapshot) {
                   // Toast.makeText(context,p0.child("date").value.toString(),Toast.LENGTH_SHORT).show()
                    (dates as ArrayList<String>).add(p0.child("date").value.toString())
                    (status as ArrayList<String>).add(p0.child("status").value.toString())

                   count ++
                   if (count == o.size){
                       val orderAdapter = OrderAdapter(o, titles, imgs,
                           dates as ArrayList<String>,
                           status as ArrayList<String>,
                           this@OrderFragment,
                           context!!)
                       orderRecyclerView.adapter = orderAdapter
                       pb.visibility = ProgressBar.GONE
                   }
                }
            })

        }
    }

    override fun toDetails(orderId: String) {
        (activity as MainActivity).gotoDetail(orderId)
    }
}