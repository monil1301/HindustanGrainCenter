package com.shah.hindustangraincenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class OrderDetailAdapter(private val list: List<AddCart>,
                         private val ratings: List<Float>,
        private val onSubmitRating: OnSubmitRating): RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>() {

    interface OnSubmitRating {
        fun submit(cart: AddCart, rating: Float)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_detail_layout,parent,false)
        return OrderDetailViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        val cart = list[position]

        holder.title.text = cart.name
        Picasso.get()
            .load(cart.imageUrl)
            .into(holder.img)
        holder.price.text = "Rs " + cart.price
        holder.quantity.text = cart.quantity
        holder.productRatingBar.rating = ratings[position]

        holder.submitButton.setOnClickListener {
            val rating = holder.productRatingBar.rating
            onSubmitRating.submit(cart,rating)
        }
    }

    class OrderDetailViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        val img = itemView.findViewById(R.id.review_image) as ImageView
        val title = itemView.findViewById(R.id.review_title) as TextView
        val price = itemView.findViewById(R.id.review_price) as TextView
        val quantity = itemView.findViewById(R.id.review_quantity) as TextView
        val productRatingBar = itemView.findViewById(R.id.product_rating) as RatingBar
        val submitButton = itemView.findViewById(R.id.rating_submit) as Button
    }
}