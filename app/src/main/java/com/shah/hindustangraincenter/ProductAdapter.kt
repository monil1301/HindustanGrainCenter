package com.shah.hindustangraincenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class ProductAdapter(private val list: List<Download>,
                     private val keys: List<String>,
                     private val cart: List<String>,
                     private val onClickListener: ProductClick,
                     private val ctx: Context):
    RecyclerView.Adapter<ProductViewHolder>() {

    interface ProductClick{
        fun productDescription(download: Download, key: String,t: String)
        fun productBuy(
            download: Download,
            buy: Button,
            s: String
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_layout,parent,false)
        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val download = list[position]

        for (i in cart){
            if (i == keys[position]){
                holder.buy.text = ctx.getString(R.string.go_to_cart)
            }
        }
        holder.title.text = download.name
        Picasso.get()
            .load( download.imageUrl)
            .into(holder.image)
        holder.price.text = ctx.getString(R.string.pricing,download.price)
        holder.info.setOnClickListener {
            onClickListener.productDescription(download,keys[position],holder.buy.text.toString())
        }
        holder.buy.setOnClickListener {
            onClickListener.productBuy(download,holder.buy,keys[position])
        }
       // Toast.makeText(context,download.imageUrl.toString(), Toast.LENGTH_LONG).show()
    }

}