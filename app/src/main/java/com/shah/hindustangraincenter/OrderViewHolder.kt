package com.shah.hindustangraincenter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OrderViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    val img = itemView.findViewById(R.id.order_img) as ImageView
    val title = itemView.findViewById(R.id.order_title) as TextView
    val date = itemView.findViewById(R.id.date) as TextView
    val status = itemView.findViewById(R.id.status) as TextView
}