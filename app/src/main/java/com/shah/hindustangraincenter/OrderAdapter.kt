package com.shah.hindustangraincenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class OrderAdapter(private val o: ArrayList<String>,
                   private val titles: ArrayList<String>,
                   private val imgs: ArrayList<String>,
                   private val dates: ArrayList<String>,
                   private val status: ArrayList<String>,
                   private val click: Onclick,
                   private val ctx: Context): RecyclerView.Adapter<OrderViewHolder>() {

    interface Onclick{
        fun toDetails(orderId: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.order_layout,parent,false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int {
        return o.size
    }

    override fun onBindViewHolder(holder: OrderViewHolder, pos: Int) {
        val position = (o.size-1) - pos
        holder.title.text = titles[position]
        Picasso.get()
            .load(imgs[position])
            .into(holder.img)
        holder.date.text = ctx.getString(R.string.order_on,dates[position])

        if (status[position] == "Cancelled"){
            holder.status.setTextColor(ContextCompat.getColor(ctx,R.color.red))
        } else {
            holder.status.setTextColor(ContextCompat.getColor(ctx,R.color.green))
        }
        holder.status.text = status[position]
        holder.itemView.setOnClickListener {
            click.toDetails(o[position])
        }
    }
}