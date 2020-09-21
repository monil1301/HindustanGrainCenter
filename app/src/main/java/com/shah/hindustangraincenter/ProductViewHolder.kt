package com.shah.hindustangraincenter

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val title = itemView.findViewById(R.id.product_title) as TextView
    val image = itemView.findViewById(R.id.product_image) as ImageView
    val price = itemView.findViewById(R.id.product_price) as TextView
    val info = itemView.findViewById(R.id.product_info) as ImageButton
    val buy = itemView.findViewById(R.id.product_buy) as Button
}