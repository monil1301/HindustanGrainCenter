package com.shah.hindustangraincenter

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView

class OrderReviewViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    val img = itemView.findViewById(R.id.review_image) as ImageView
    val title = itemView.findViewById(R.id.review_title) as TextView
    val price = itemView.findViewById(R.id.review_price) as TextView
    val quantity = itemView.findViewById(R.id.review_quantity) as TextView
}