package com.shah.hindustangraincenter

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HomeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val header = itemView.findViewById(R.id.title_home_layout) as TextView
    val img1 = itemView.findViewById(R.id.img_1) as ImageView
    val img2 = itemView.findViewById(R.id.img_2) as ImageView
    val title1 = itemView.findViewById(R.id.title_1) as TextView
    val title2 = itemView.findViewById(R.id.title_2) as TextView
    val price1 = itemView.findViewById(R.id.price_1) as TextView
    val price2 = itemView.findViewById(R.id.price_2) as TextView
    val viewAll = itemView.findViewById(R.id.view_all_button) as Button
}