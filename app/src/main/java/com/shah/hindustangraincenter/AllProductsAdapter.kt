package com.shah.hindustangraincenter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AllProductsAdapter(private val folder: List<String>,
                         private val pro: List<Download>,
                         private val keys: List<String>,
                         private val cart: List<String>,
                         private val ctx: Context,
                         private val onClickListener: ProductClick
): RecyclerView.Adapter<AllProductsAdapter.AllProVH>(), Filterable {

    private var allList: ArrayList<Download>? = null

    interface ProductClick{
        fun productDescription(download: Download, k: String,t: String,name: String)
        fun productBuy(
            download: Download,
            buy: Button,
            k: String,
            name: String
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProVH {
        allList = ArrayList<Download>()
        (allList as ArrayList<Download>).addAll(pro)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_layout,parent,false)
        return AllProVH(view)
    }

    override fun getItemCount(): Int {
        return pro.size
    }

    override fun onBindViewHolder(holder: AllProVH, position: Int) {
        val download = pro[position]

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
            onClickListener.productDescription(download,
                keys[position],
                holder.buy.text.toString(),
                folder[position])
        }
        holder.buy.setOnClickListener {
            onClickListener.productBuy(download,holder.buy,keys[position],folder[position])
        }
    }

    class AllProVH(itemView: View): RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById(R.id.product_title) as TextView
        val image = itemView.findViewById(R.id.product_image) as ImageView
        val price = itemView.findViewById(R.id.product_price) as TextView
        val info = itemView.findViewById(R.id.product_info) as ImageButton
        val buy = itemView.findViewById(R.id.product_buy) as Button
    }

    override fun getFilter(): Filter {
        return filter
    }

    private val filter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResult: FilterResults = FilterResults()
            filterResult.values = allList?.filter{ it.name!!.contains(constraint.toString().toLowerCase()) }
            return filterResult
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            (pro as ArrayList).clear()
            pro.addAll(results?.values as Collection<Download>)
//            Toast.makeText(ctx,allList.toString(),Toast.LENGTH_LONG).show()
            notifyDataSetChanged()
        }

    }
}
