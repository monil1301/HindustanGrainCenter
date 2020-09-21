package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProductsFragment : Fragment(), ProductAdapter.ProductClick {

    private lateinit var productRecyclerView: RecyclerView
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var downloadProgressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    private var downloads: List<Download>? = null
    private var keys: List<String>? = null
    private lateinit var uid: String

    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_products, container, false)
        productRecyclerView = root.findViewById(R.id.product_rv)
        shimmer = root.findViewById(R.id.shimmer)
        downloadProgressBar = root.findViewById(R.id.download_progress_bar)

        downloadProgressBar.visibility = ProgressBar.VISIBLE

        val n = arguments?.get("name")

        storageRef = FirebaseStorage.getInstance().getReference(n.toString())
        databaseRef = FirebaseDatabase.getInstance().getReference(n.toString())
        auth = FirebaseAuth.getInstance()

        uid = auth.uid!!
        downloads = ArrayList()
        keys = ArrayList()

        productRecyclerView.layoutManager = LinearLayoutManager(context)
        //Toast.makeText(context,x.toString(),Toast.LENGTH_LONG).show()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                downloadProgressBar.visibility = ProgressBar.GONE
                Toast.makeText(context, p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (downloads as ArrayList<Download>).clear()
                (keys as ArrayList<String>).clear()
                for (x in p0.children) {
                    // Toast.makeText(context,x.toString(), Toast.LENGTH_LONG).show()
                    val description = x.getValue(Download::class.java)
                    (downloads as ArrayList<Download>).add(description!!)
                    (keys as ArrayList<String>).add(x.key.toString())
                }

                showItem(
                    downloads as ArrayList<Download>,
                    keys as ArrayList<String>
                )
            }
        })
        return root
    }

    @SuppressLint("InflateParams")
    override fun productDescription(
        download: Download,
        key: String,
        t: String
    ) {
        val layout = LayoutInflater.from(context).inflate(R.layout.product_descrition_layout, null)

        layout.findViewById<TextView>(R.id.description_title).text = download.name
        layout.findViewById<TextView>(R.id.description_price).text = getString(R.string.pricing,download.price)

        Picasso.get()
            .load(download.imageUrl)
            .into(layout.findViewById<ImageView>(R.id.description_image))

        val desc = download.description?.split(",")
        if (desc != null) {
            var description = ""
            for (i in desc){
                description += "- ${i.trim().capitalize()}\n"
            }
            layout.findViewById<TextView>(R.id.description_description).text = description
        }

        val buy = layout.findViewById<Button>(R.id.description_buy)
        val cancel = layout.findViewById<Button>(R.id.description_cancel)
        buy.text = t

        val builder = AlertDialog.Builder(context)
            .setView(layout)
        val alert = builder.show()

        buy.setOnClickListener {
            if (buy.text.toString().trim() == "Buy") {
                val n = arguments?.get("name")
                //databaseRef = FirebaseDatabase.getInstance().getReference("$n/$key")
                val g = "$n/$key/price"
                buy.text = getString(R.string.go_to_cart)
                databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
                val cart = AddCart(
                    download.name,
                    download.imageUrl,
                    download.price,
                    1.toString(),
                    g.toString(),
                    key,
                    download.price
                )
                databaseRef.push().setValue(cart)
            } else {
                (activity as MainActivity).goto(1)
                alert.dismiss()
            }
        }

        cancel.setOnClickListener {
            alert.dismiss()
        }
    }

    override fun productBuy(
        download: Download,
        buy: Button,
        s: String
    ) {
        val n = arguments?.get("name")
        if (buy.text.toString().trim() == "Buy") {
            val g = "$n/$s/price"
            //Toast.makeText(context,g.toString(),Toast.LENGTH_SHORT).show()
            buy.text = getString(R.string.go_to_cart)
            databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
            val cart = AddCart(
                download.name,
                download.imageUrl,
                download.price,
                1.toString(),
                g.toString(),
                s,
                download.price
            )
            databaseRef.push().setValue(cart)
        } else {
               (activity as MainActivity).goto(1)
        }
    }

    private fun showItem(
        d: ArrayList<Download>,
        k: ArrayList<String>
    ) {
        databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")

        val cart: List<String>?
        cart = ArrayList()
        cart.clear()


        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                cart.clear()
                //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()
                for (x in p0.children) {
                    val description = x.getValue(AddCart::class.java)
                    cart.add(description!!.id!!)
                }
                val productAdapter = ProductAdapter(
                    d,
                    k,
                    cart,
                    this@ProductsFragment,
                    context!!
                )

                productRecyclerView.adapter = productAdapter
                downloadProgressBar.visibility = ProgressBar.GONE
                shimmer.stopShimmer()
                shimmer.visibility = View.GONE
                productRecyclerView.visibility = RecyclerView.VISIBLE
            }
        })
    }
}