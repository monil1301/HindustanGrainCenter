package com.shah.hindustangraincenter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var uid: String
    private var cart: List<AddCart>? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var cartFab: FloatingActionButton
    private lateinit var badge: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        if (toolbar != null){
            setSupportActionBar(toolbar)
        }

        window.statusBarColor = getColor(R.color.colorPrimaryDark)

        cartFab = findViewById(R.id.cart_fab)
        badge = findViewById(R.id.badge)

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(this,R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration.Builder(
            R.id.menu_home,
            R.id.menu_cart,
            R.id.menu_acc,
            R.id.menu_orders,
            R.id.menu_help,
            R.id.menu_logout
        ).setDrawerLayout(drawerLayout).build()

        setupActionBarWithNavController(this, navController)
        NavigationUI.setupWithNavController(navView,navController)

        navView.setNavigationItemSelectedListener(this)
        auth = FirebaseAuth.getInstance()
        uid = auth.uid!!

        cart = ArrayList()
        databaseRef = FirebaseDatabase.getInstance().getReference("cart/$uid/")
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@MainActivity,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (cart as ArrayList).clear()
                for (x in p0.children){
                    val description = x.getValue(AddCart::class.java)
                    (cart as ArrayList<AddCart>).add(description!!)
                }

                var q = 0
                for (i in cart as ArrayList<AddCart>) {
                    q += i.quantity!!.toInt()
                }
                if (q > 0){
                    if (q < 10){
                        badge.visibility = TextView.VISIBLE
                        badge.text = getString(R.string.badge0,q)
                    } else{
                        badge.visibility = TextView.VISIBLE
                        badge.text = q.toString()
                    }
                } else{
                    badge.visibility = TextView.GONE
                }
            }
        })

        cartFab.setOnClickListener {
            navController.navigate(R.id.cart_fragment )
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(this, R.id.nav_host_fragment)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        when (item.itemId) {
            R.id.menu_home -> navController.navigate(R.id.home_fragment)
            R.id.menu_cart -> navController.navigate(R.id.cart_fragment)
            R.id.menu_acc -> navController.navigate(R.id.account_fragment)
            R.id.menu_orders -> navController.navigate(R.id.order_fragment)
            R.id.menu_help -> navController.navigate(R.id.help_fragment)
            R.id.menu_logout -> logout()
        }
        drawerLayout.closeDrawers()
        return true
    }

    private fun logout() {
        auth = FirebaseAuth.getInstance()

        auth.signOut()
        Toast.makeText(this,"Logged out successfully",Toast.LENGTH_SHORT).show()
        val i = Intent(this,LoginActivity::class.java)
        startActivity(i)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(this,R.id.nav_host_fragment)
        val x = navController.currentDestination?.id
        if (x == R.id.pod_fragment){
            navController.navigate(R.id.home_fragment)
        }
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun gotoProducts(download: String){
        val bundle = bundleOf(Pair("name",download))
        val navController = findNavController(this,R.id.nav_host_fragment)
        navController.navigate(R.id.product_fragment,bundle)
    }

    fun goto(int: Int){
        val navController = findNavController(this,R.id.nav_host_fragment)
        when(int){
            1 -> navController.navigate(R.id.cart_fragment)
            2 -> navController.navigate(R.id.pass_fragment)
            3 -> navController.navigate(R.id.manage_add_fragment)
            4 -> navController.navigate(R.id.delivery_add_fragment)
            5 -> navController.navigate(R.id.order_review_fragment)
            6 -> navController.navigate(R.id.payment_fragment)
            7 -> navController.navigate(R.id.home_fragment)
        }
    }

    fun gotoPod(p: Int){
        val bundle = bundleOf(Pair("price",p))
        val navController = findNavController(this,R.id.nav_host_fragment)
        navController.navigate(R.id.pod_fragment,bundle)
    }

    fun gotoDetail(orderId: String) {
        val bundle = bundleOf(Pair("orderId",orderId))
        val navController = findNavController(this,R.id.nav_host_fragment)
        navController.navigate(R.id.order_detail_fragment,bundle)
    }

    fun showDrawer(){
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {

    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.exit_menu,menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.exit){
//            finish()
//            exitProcess(0)
//        }
//
//
//        return super.onOptionsItemSelected(item)
//    }

}
