package com.shah.hindustangraincenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class CartAdapter(private val list: List<AddCart>,
                  private val keys: List<String>,
                  private val onclick: CartOnClick
                  ): RecyclerView.Adapter<CartViewHolder>() {

    interface CartOnClick{
        fun removeItem(key: String,position: Int)
        fun updateItem(key: String, cPrice: Int, cQuantity: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_layout,parent,false)
        return CartViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cart = list[position]
        holder.title.text = cart.name
        Picasso.get()
            .load(cart.imageUrl)
            .into(holder.img)

        holder.price.text = "Rs ${cart.price}"
        holder.quantity.setText(cart.quantity)

        holder.remove.setOnClickListener {
            onclick.removeItem(keys[position],position)
        }

        holder.plus.setOnClickListener {
            val cQuantity = cart.quantity!!.toInt() + 1
            val cPrice =  cart.cost!!.toInt() * cQuantity
            onclick.updateItem(keys[position],cPrice,cQuantity)
        }

        holder.minus.setOnClickListener {
            val cQuantity = cart.quantity!!.toInt() - 1
            if (cQuantity > 0){
                val cPrice = cart.cost!!.toInt() * cQuantity
                onclick.updateItem(keys[position],cPrice,cQuantity)
            }
        }
    }
}