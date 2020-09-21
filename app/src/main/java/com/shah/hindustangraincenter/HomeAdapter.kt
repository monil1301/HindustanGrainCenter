package com.shah.hindustangraincenter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class HomeAdapter(private val folder: List<String>,
                  private val f: List<List<Download>>,
                  private val keys: List<List<String>>,
                  private val cart: List<String>,
                  private val onClick: HomeClick,
                  private val ctx: Context?
): RecyclerView.Adapter<HomeViewHolder>() {

    interface HomeClick{
        fun gotoPro(name: String)
        fun productDescription(
            download: Download,
            k: String,
            t: String,
            name: String
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.home_layout,parent,false)
        return HomeViewHolder(v)
    }

    override fun getItemCount(): Int {
        return folder.size
    }

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        holder.header.text = folder[position]
        val x = f[position]

        val k = keys[position]
        for (i in 0..1){
            if (i == 0){
                val download = x[0]
                Picasso.get()
                    .load( download.imageUrl)
                    .into(holder.img1)
                holder.title1.text = download.name
                holder.price1.text = ctx?.getString(R.string.pricing, download.price)
                holder.img1.setOnClickListener {
                    //Toast.makeText(ctx,cart.toString(),Toast.LENGTH_SHORT).show()
                    for (j in cart){
                        if (j == k[0]){
                            val t = "go to cart"
                            onClick.productDescription(download,k[0],t,folder[position])
                            return@setOnClickListener
                        }
                    }
                    onClick.productDescription(download,k[0],"Buy",folder[position])
                }
            }else{
                val download = x[1]
                Picasso.get()
                    .load( download.imageUrl)
                    .into(holder.img2)
                holder.title2.text = download.name
                holder.price2.text = ctx?.getString(R.string.pricing, download.price)
                holder.img2.setOnClickListener {
                    for (j in cart){
                        if (j == k[1]){
                            val t = "go to cart"
                            onClick.productDescription(download,k[0],t,folder[position])
                            return@setOnClickListener
                        }
                    }
                    onClick.productDescription(download,k[1],"Buy",folder[position])
                }
            }
        }
        holder.viewAll.setOnClickListener {
            onClick.gotoPro(folder[position])
        }
    }
}