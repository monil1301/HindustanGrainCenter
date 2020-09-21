package com.shah.hindustangraincenter

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartFragment: Fragment(), CartAdapter.CartOnClick {

    private lateinit var progressBar: ProgressBar
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var price: TextView
    private lateinit var quantity: TextView
    private lateinit var placeOrder: Button

    private var cart: List<AddCart>? = null
    private var keys: List<String>? = null
    private var cartAdapter: CartAdapter? = null

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_cart,container,false)
        progressBar = root.findViewById(R.id.cart_progress_bar)
        cartRecyclerView = root.findViewById(R.id.cart_rv)
        price = root.findViewById(R.id.total_price)
        quantity = root.findViewById(R.id.total_quantity)
        placeOrder = root.findViewById(R.id.place_order)

        progressBar.visibility = ProgressBar.VISIBLE
        auth = FirebaseAuth.getInstance()
        uid = auth.uid!!

        this.cart = ArrayList()
        this.keys = ArrayList()

        cartRecyclerView.layoutManager = LinearLayoutManager(context)

        databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
        showItem()
        updatePro()

        placeOrder.setOnClickListener {
            progressBar.visibility = ProgressBar.VISIBLE
            if (cart.isNullOrEmpty()) {
                Toast.makeText(context, "Your cart is empty", Toast.LENGTH_LONG).show()
            } else {
                (activity as MainActivity).goto(4)
            }
        }

        val swipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                if (direction == ItemTouchHelper.LEFT) {
                   removeItem((keys as ArrayList<String>)[viewHolder.adapterPosition],viewHolder.adapterPosition)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper (swipe)
        itemTouchHelper.attachToRecyclerView(cartRecyclerView)
            return root
    }

    private fun showItem(){
        (cart as ArrayList).clear()
        (keys as ArrayList).clear()


        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()
                for (x in p0.children){
                    (keys as ArrayList<String>).add(x.key!!)
                    val description = x.getValue(AddCart::class.java)
                    (cart as ArrayList<AddCart>).add(description!!)
                }
                cartAdapter = CartAdapter(
                    cart as ArrayList<AddCart>,
                    keys as ArrayList<String>,
                    this@CartFragment
                )

                cartRecyclerView.adapter = cartAdapter

                updatePriceQuantity(cart as ArrayList<AddCart>, keys as ArrayList<String>)
            }
        })
    }

    override fun removeItem(key: String,position: Int) {
        databaseRef.child(key).removeValue()
        (cart as ArrayList<AddCart>).removeAt(position)
        (keys as ArrayList<String>).removeAt(position)
        updatePriceQuantity(cart as ArrayList<AddCart>, keys as ArrayList<String>)
        cartAdapter?.notifyItemRemoved(position)
    }

    override fun updateItem(key: String, cPrice: Int, cQuantity: Int) {
        progressBar.visibility = ProgressBar.VISIBLE
        databaseRef.child(key).child("quantity").setValue(cQuantity.toString())
        databaseRef.child(key).child("price").setValue(cPrice.toString())
    }

    private fun updatePro() {
        Log.d("cart","updatePro called")
        databaseRef.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            // (cart as ArrayList<AddCart>).add(index!!,p0.getValue(AddCart::class.java))
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            // (cart as ArrayList<AddCart>).add(index!!,p0.getValue(AddCart::class.java))
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("cart","works")
                val k = p0.key
                val index = this@CartFragment.keys?.indexOf(k)
                if (index != null) {
                    if (index >= 0) {
                        (cart as ArrayList<AddCart>)[index] = p0.getValue(AddCart::class.java)!!
                        cartAdapter?.notifyDataSetChanged()
                        updatePriceQuantity(cart as ArrayList<AddCart>, keys as ArrayList<String>)
                    }
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
            //   Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            // (cart as ArrayList<AddCart>).add(index!!,p0.getValue(AddCart::class.java))
            }
        })
    }

    private fun updatePriceQuantity(
        cart: ArrayList<AddCart>,
        keys: ArrayList<String>
    ) {
        for (i in cart.indices) {
           val s = cart[i]
            val k = keys[i]

            FirebaseDatabase.getInstance().getReference(s.costRef!!).
                addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.message, Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                val cp = p0.value.toString().toInt() * s.quantity!!.toInt()
                FirebaseDatabase.getInstance().getReference("cart/$uid/$k")
                    .child("price").setValue(cp.toString())
                FirebaseDatabase.getInstance().getReference("cart/$uid/$k")
                    .child("cost").setValue(p0.value.toString())
            }

        })
        }

        this.cart = cart
        this.keys = keys

        var q = 0
        var p = 0
        for (i in this.cart as ArrayList<AddCart>) {
            q += i.quantity!!.toInt()
            p += i.price!!.toInt()
        }
        quantity.text = q.toString()
        price.text = "Rs $p"
        progressBar.visibility = ProgressBar.GONE
    }
}