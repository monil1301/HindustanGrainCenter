package com.shah.hindustangraincenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class OrderReviewAdapter(private val list: List<AddCart>): RecyclerView.Adapter<OrderReviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_rewiew_layout,parent,false)
        return OrderReviewViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OrderReviewViewHolder, position: Int) {
        val cart = list[position]
        holder.title.text = cart.name
        Picasso.get()
            .load(cart.imageUrl)
            .into(holder.img)
        holder.price.text = "Rs " + cart.price
        holder.quantity.text = cart.quantity
    }
}