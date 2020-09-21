package com.shah.hindustangraincenter

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class CartViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val img = itemView.findViewById(R.id.cart_image) as ImageView
    val title = itemView.findViewById(R.id.cart_title) as TextView
    val price = itemView.findViewById(R.id.cart_price) as TextView
    val minus = itemView.findViewById(R.id.cart_minus) as ImageButton
    val plus = itemView.findViewById(R.id.cart_plus) as ImageButton
    val quantity = itemView.findViewById(R.id.cart_quantity) as EditText
    val remove = itemView.findViewById(R.id.cart_remove) as Button
}