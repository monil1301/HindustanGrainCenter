package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class HomeFragment : Fragment(), HomeAdapter.HomeClick, AllProductsAdapter.ProductClick {

    private lateinit var header: TextView
    private lateinit var homeRecyclerView: RecyclerView
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var downloadProgressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    private var cart: List<String>? = null
    private var folders: List<String>? = null
    private var keys: List<List<String>>? = null
    private var d: List<List<Download>>? = null
    private lateinit var uid: String
    private var productsAdapter: AllProductsAdapter? = null

    private lateinit var storageRef: StorageReference
    private lateinit var databaseRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        header = root.findViewById(R.id.home_header)
        homeRecyclerView = root.findViewById(R.id.home_rv)
        shimmer = root.findViewById(R.id.shimmer)
        downloadProgressBar = root.findViewById(R.id.download_progress_bar)
        refresh = root.findViewById(R.id.home_refresh)

        val ab = (activity as MainActivity).supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        ab?.setHomeAsUpIndicator(R.drawable.nav_icon)

        auth = FirebaseAuth.getInstance()
        header.text = getString(R.string.welcome, auth.currentUser?.displayName)
        setHasOptionsMenu(true)

        //isVerified()

        downloadProgressBar.visibility = ProgressBar.VISIBLE

        folders = ArrayList()
        d = ArrayList()
        keys = ArrayList()

        uid = auth.uid!!
        storageRef = FirebaseStorage.getInstance().reference
        databaseRef = FirebaseDatabase.getInstance().reference

        show()
        getFolder()
        homeRecyclerView.layoutManager = LinearLayoutManager(context)

        refresh.setOnRefreshListener {
            refresh.isRefreshing = false
        }
        //Toast.makeText(context,x.toString(),Toast.LENGTH_LONG).show(
        return root
    }

    private fun getFolder() {
        databaseRef = FirebaseDatabase.getInstance().getReference("products")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.toString(), Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (folders as ArrayList<String>).clear()
                for (x in p0.children) {
                    (folders as ArrayList<String>).add(x.value.toString())
                }

                display(folders as ArrayList<String>)
            }
        })

    }

    private fun isVerified() {
        val em = auth.currentUser?.email
        if (!auth.currentUser!!.isEmailVerified) {
            val tv = TextView(context)
            tv.text = getString(R.string.verify_email)
            AlertDialog.Builder(context)
                .setTitle("Email not verified!")
                .setView(tv)
                .setPositiveButton("Get verified") { dialog, _ ->
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            tv.text = getString(R.string.verification_sent, em)
                        }
                }
                .create().show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            (activity as MainActivity).showDrawer()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun display(folders: ArrayList<String>) {
        var n = 0
        for (i in folders) {

            //Toast.makeText(context, (folders as ArrayList<String>).size.toString(),Toast.LENGTH_LONG).show()
            databaseRef = FirebaseDatabase.getInstance().getReference(i)

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    downloadProgressBar.visibility = ProgressBar.GONE
                    Toast.makeText(context, p0.toString(), Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()

                    val y = ArrayList<Download>()
                    val w = ArrayList<String>()
                    for (x in p0.children) {

                        val description = x.getValue(Download::class.java)
                        y.add(description!!)
                        w.add(x.key.toString())
                    }
                    n += 1
                    (keys as ArrayList<List<String>>).add(w)
                    (d as ArrayList<List<Download>>).add(y)
                    // Toast.makeText(context,(d as ArrayList<List<Download>>).toString(),Toast.LENGTH_LONG).show()
                    if (n >= ((folders).size)) {

//                        show(folders,
//                            d as ArrayList<List<Download>>,
//                            keys as ArrayList<List<String>>
//                        )

                        val homeAdapter = HomeAdapter(
                            folders,
                            d as ArrayList<List<Download>>,
                            keys as ArrayList<List<String>>,
                            cart as ArrayList<String>,
                            this@HomeFragment,
                            context
                        )
                        homeRecyclerView.adapter = homeAdapter
                        downloadProgressBar.visibility = ProgressBar.GONE
                        shimmer.stopShimmer()
                        shimmer.visibility = View.GONE
                        homeRecyclerView.visibility = RecyclerView.VISIBLE
                    }
                }
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.exit_menu, menu)
        val item = menu.findItem(R.id.go)
        val searchView = item?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productsAdapter?.filter?.filter(newText)
                return true
            }
        })

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                downloadProgressBar.visibility = ProgressBar.VISIBLE
                getProductsNames()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                downloadProgressBar.visibility = ProgressBar.VISIBLE
                getFolder()
                return true
            }

        })
    }

    private fun show() {
        databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")

        this.cart = ArrayList()

        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (cart as ArrayList<String>).clear()
                //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()
                for (x in p0.children) {
                    val description = x.getValue(AddCart::class.java)
                    (this@HomeFragment.cart as ArrayList<String>).add(description!!.id!!)
                }

//                val homeAdapter = HomeAdapter(
//                    f,
//                    d,
//                    keys,
//                    cart as ArrayList<String>,
//                    this@HomeFragment,
//                    context
//                )
//                homeRecyclerView.adapter = homeAdapter
//                downloadProgressBar.visibility = ProgressBar.GONE
//                shimmer.stopShimmer()
//                shimmer.visibility = View.GONE
//                homeRecyclerView.visibility = RecyclerView.VISIBLE

            }
        })
    }

    override fun gotoPro(name: String) {
        (activity as MainActivity).gotoProducts(name)
    }

    @SuppressLint("InflateParams")
    override fun productDescription(
        download: Download,
        k: String,
        t: String,
        name: String
    ) {
        val layout = LayoutInflater.from(context).inflate(R.layout.product_descrition_layout, null)

        layout.findViewById<TextView>(R.id.description_title).text = download.name
        layout.findViewById<TextView>(R.id.description_price).text =
            getString(R.string.pricing, download.price)

        Log.d("productKey", k)

        databaseRef = FirebaseDatabase.getInstance().getReference("rating/$k/")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.message, Toast.LENGTH_SHORT).show()
                return
            }

            override fun onDataChange(p0: DataSnapshot) {
                var sum = 0.0
                var ratings = 0
                for (x in p0.children) {
                    sum += x.value.toString().toFloat()
                    ratings++
                }
                if ((sum / ratings).isNaN()) {
                    layout.findViewById<TextView>(R.id.description_rating).text =
                        "No Ratings"
                } else {
                    layout.findViewById<TextView>(R.id.description_rating).text =
                        (sum / ratings).toString()
                }
            }
        })

        Picasso.get()
            .load(download.imageUrl)
            .into(layout.findViewById<ImageView>(R.id.description_image))

        val desc = download.description?.split(",")
        if (desc != null) {
            var description = ""
            for (i in desc) {
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

        val g = "$name/$k/price"

        buy.setOnClickListener {
            if (buy.text.toString().trim() == "Buy") {
                buy.text = getString(R.string.go_to_cart)
                databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
                val cart = AddCart(
                    download.name,
                    download.imageUrl,
                    download.price,
                    1.toString(),
                    g,
                    k,
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

    override fun onResume() {
        //isVerified()
        super.onResume()
    }

    private fun getProductsNames() {
        var f: List<String>? = null
        f = ArrayList<String>()
        databaseRef = FirebaseDatabase.getInstance().getReference("products")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, p0.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (f as ArrayList<String>).clear()
                for (x in p0.children) {
                    (f as ArrayList<String>).add(x.value.toString())
                }

                getProductsList(f as ArrayList<String>)
            }
        })
    }

    private fun getProductsList(f: ArrayList<String>) {
        val y = ArrayList<Download>()
        val w = ArrayList<String>()
        var n = 0
        for (i in f) {
            databaseRef = FirebaseDatabase.getInstance().getReference(i)

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(requireContext(), p0.toString(), Toast.LENGTH_LONG).show()
                }

                override fun onDataChange(p0: DataSnapshot) {
                    //Toast.makeText(context,p0.toString(),Toast.LENGTH_LONG).show()


                    for (x in p0.children) {

                        val description = x.getValue(Download::class.java)
                        (y as ArrayList<Download>).add(description!!)
                        w.add(x.key.toString())
                    }
                    n++

                    if (n == f.size) {
                        this@HomeFragment.productsAdapter = AllProductsAdapter(
                            f,
                            y as ArrayList<Download>,
                            w,
                            cart!!,
                            requireContext(),
                            this@HomeFragment
                        )
                        homeRecyclerView.adapter = productsAdapter
                        downloadProgressBar.visibility = ProgressBar.GONE
                    }
                }
            })
        }

    }

    override fun productBuy(download: Download, buy: Button, k: String, name: String) {
        val g = "$name/$k/price"

        if (buy.text.toString().trim() == "Buy") {
            buy.text = getString(R.string.go_to_cart)
            databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
            val cart = AddCart(
                download.name,
                download.imageUrl,
                download.price,
                1.toString(),
                g.toString(),
                k,
                download.price
            )
            databaseRef.push().setValue(cart)
        } else {
            (activity as MainActivity).goto(1)
        }
    }
}