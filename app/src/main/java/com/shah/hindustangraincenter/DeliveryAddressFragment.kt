package com.shah.hindustangraincenter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DeliveryAddressFragment: Fragment(), AddressAdapter.Checks {

    private lateinit var newAdd: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var manageRecyclerView: RecyclerView
    private lateinit var deliver: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var uid: String
    private var addresses: List<Address>? = null
    private var keys: List<String>? = null
    private var deliverAddress: Address? = null
    private var adapter: AddressAdapter? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_delivery_address,container,false)
        newAdd = root.findViewById(R.id.new_address)
        progressBar = root.findViewById(R.id.pb)
        manageRecyclerView = root.findViewById(R.id.manage_add_rv)
        deliver = root.findViewById(R.id.deliver)

        auth = FirebaseAuth.getInstance()
        uid = auth.currentUser!!.uid
        databaseRef = FirebaseDatabase.getInstance().getReference("address/$uid/")
        manageRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        addresses = ArrayList()
        keys = ArrayList()

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context,p0.toString(), Toast.LENGTH_LONG).show()
            }

            override fun onDataChange(p0: DataSnapshot) {
                (addresses as ArrayList<Address>).clear()
                (keys as ArrayList<String>).clear()
                for (x in p0.children){
                    (keys as ArrayList<String>).add(x.key!!)
                    val i = x.getValue(Address::class.java)
                    (addresses as ArrayList<Address>).add(i!!)
                }
                this@DeliveryAddressFragment.adapter = AddressAdapter(addresses as ArrayList<Address>,
                    this@DeliveryAddressFragment)
                manageRecyclerView.adapter = adapter
                progressBar.visibility = ProgressBar.GONE
            }

        })

        updateRV()

        newAdd.setOnClickListener {
            //            Toast.makeText(requireContext(),"Add new Address",Toast.LENGTH_SHORT).show()

            val layout = LayoutInflater.from(requireContext()).inflate(R.layout.new_add_layout,null)
            val name = layout.findViewById<TextInputEditText>(R.id.name)
            val addr1 = layout.findViewById<TextInputEditText>(R.id.addr1)
            val addr2 = layout.findViewById<TextInputEditText>(R.id.addr2)
            val land = layout.findViewById<TextInputEditText>(R.id.landmark)
            val city = layout.findViewById<TextInputEditText>(R.id.city)
            val state = layout.findViewById<TextInputEditText>(R.id.state)
            val pin = layout.findViewById<TextInputEditText>(R.id.pin)
            val cancel = layout.findViewById<Button>(R.id.cancel_add)
            val save = layout.findViewById<Button>(R.id.save_add)
            val alert = AlertDialog.Builder(context)
                .setView(layout)

            val builder = alert.show()
            cancel.setOnClickListener {
                builder.dismiss()
            }

            save.setOnClickListener {
                if (name.text.isNullOrEmpty()){
                    name.error = "Field should not be empty"
                    name.requestFocus()
                } else if (addr1.text.isNullOrEmpty()){
                    addr1.error = "Field should not be empty"
                    addr1.requestFocus()
                } else if (addr2.text.isNullOrEmpty()){
                    addr2.error = "Field should not be empty"
                    addr2.requestFocus()
                } else if (city.text.isNullOrEmpty()){
                    city.error = "Field should not be empty"
                    city.requestFocus()
                } else if (state.text.isNullOrEmpty()) {
                    state.error = "Field should not be empty"
                    state.requestFocus()
                } else if (pin.text.isNullOrEmpty() || pin.text.toString().trim().length != 6){
                    pin.error = "Invalid pin code"
                    pin.requestFocus()
                } else {
                    if (land.text.isNullOrEmpty()) {
                        val address = Address(
                            name.text.toString().trim(),
                            addr1.text.toString().trim(),
                            addr2.text.toString().trim(),
                            city.text.toString().trim(),
                            state.text.toString().trim(),
                            pin.text.toString().trim(),
                            true,
                            ""
                        )
                        databaseRef.push().setValue(address).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(requireContext(),"Address added successfully", Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            } else {
                                Toast.makeText(requireContext(),"Error adding address, try again",
                                    Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            }
                        }
                    } else {
                        val address = Address(
                            name.text.toString().trim(),
                            addr1.text.toString().trim(),
                            addr2.text.toString().trim(),
                            city.text.toString().trim(),
                            state.text.toString().trim(),
                            pin.text.toString().trim(),
                            true,
                            land.text.toString().trim()
                        )
                        databaseRef.push().setValue(address).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(requireContext(),"Address added successfully", Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            } else {
                                Toast.makeText(requireContext(),"Error adding address, try again",
                                    Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            }
                        }
                    }
                }
            }
        }

        deliver.setOnClickListener {
            var counter = 0
            for (i in addresses!!){
                if (i.check!!){
                    deliverAddress = i
                    (activity as MainActivity).goto(5)
                    counter++
                }
            }
            if (counter == 0) {
                Toast.makeText(context, "No address selected", Toast.LENGTH_SHORT).show()
                Log.d("deliver", "not selected")
            }
        }
        return root
    }

    override fun changeCheck(position: Int) {
        for (i in keys!!.indices){
            if (i != position){
                databaseRef.child(keys!![i]).child("check").setValue(false)
            } else{
                databaseRef.child(keys!![i]).child("check").setValue(true)
            }
        }
    }

    private fun updateRV() {
        Log.d("manageAddress", "UpdateRV called")
        databaseRef.addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                //nothing
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                //nothing
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                Log.d("manageAddress", "child change called")
                val k = p0.key
                val index = keys?.indexOf(k)
                if (index != null) {
                    (addresses as ArrayList<Address>)[index] = p0.getValue(Address::class.java)!!
                    adapter?.notifyDataSetChanged()
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val k = p0.key
                val index = keys?.indexOf(k)
                (this@DeliveryAddressFragment.keys as ArrayList<String>).add(p0.key.toString())
                (this@DeliveryAddressFragment.addresses as ArrayList<Address>).add(p0.getValue(Address::class.java)!!)
                adapter?.notifyDataSetChanged()
                Log.d("manageAddress", "child added called")
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                val k = p0.key
                val index = keys?.indexOf(k)
                if (index != null) {
                    (addresses as ArrayList<Address>).removeAt(index)
                    (keys as ArrayList<String>).removeAt(index)
                    adapter?.notifyItemRemoved(index)
                }
            }

        })
    }

    @SuppressLint("InflateParams")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 0){
            val position = item.groupId
            val adds = addresses!![position]
            val layout = LayoutInflater.from(requireContext()).inflate(R.layout.new_add_layout,null)
            val name = layout.findViewById<TextInputEditText>(R.id.name)
            val addr1 = layout.findViewById<TextInputEditText>(R.id.addr1)
            val addr2 = layout.findViewById<TextInputEditText>(R.id.addr2)
            val land = layout.findViewById<TextInputEditText>(R.id.landmark)
            val city = layout.findViewById<TextInputEditText>(R.id.city)
            val state = layout.findViewById<TextInputEditText>(R.id.state)
            val pin = layout.findViewById<TextInputEditText>(R.id.pin)
            val cancel = layout.findViewById<Button>(R.id.cancel_add)
            val save = layout.findViewById<Button>(R.id.save_add)

            name.setText(adds.name)
            addr1.setText(adds.addr1)
            addr2.setText(adds.addr2)
            city.setText(adds.city)
            state.setText(adds.state)
            pin.setText(adds.pin)

            val alert = AlertDialog.Builder(context)
                .setView(layout)

            val builder = alert.show()
            cancel.setOnClickListener {
                builder.dismiss()
            }

            save.setOnClickListener {
                if (name.text.isNullOrEmpty()){
                    name.error = "Field should not be empty"
                    name.requestFocus()
                } else if (addr1.text.isNullOrEmpty()){
                    addr1.error = "Field should not be empty"
                    addr1.requestFocus()
                } else if (addr2.text.isNullOrEmpty()){
                    addr2.error = "Field should not be empty"
                    addr2.requestFocus()
                } else if (city.text.isNullOrEmpty()){
                    city.error = "Field should not be empty"
                    city.requestFocus()
                } else if (state.text.isNullOrEmpty()) {
                    state.error = "Field should not be empty"
                    state.requestFocus()
                } else if (pin.text.isNullOrEmpty() || pin.text.toString().trim().length != 6){
                    pin.error = "Invalid pin code"
                    pin.requestFocus()
                } else {
                    if (land.text.isNullOrEmpty()) {
                        val address = Address(
                            name.text.toString().trim(),
                            addr1.text.toString().trim(),
                            addr2.text.toString().trim(),
                            city.text.toString().trim(),
                            state.text.toString().trim(),
                            pin.text.toString().trim(),
                            true,
                            ""
                        )
                        databaseRef.push().setValue(address).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(requireContext(),"Address added successfully", Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            } else {
                                Toast.makeText(requireContext(),"Error adding address, try again",
                                    Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            }
                        }
                    } else {
                        val address = Address(
                            name.text.toString().trim(),
                            addr1.text.toString().trim(),
                            addr2.text.toString().trim(),
                            city.text.toString().trim(),
                            state.text.toString().trim(),
                            pin.text.toString().trim(),
                            true,
                            land.text.toString().trim()
                        )
                        databaseRef.push().setValue(address).addOnCompleteListener {
                            if (it.isSuccessful){
                                Toast.makeText(requireContext(),"Address added successfully", Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            } else {
                                Toast.makeText(requireContext(),"Error adding address, try again",
                                    Toast.LENGTH_SHORT).show()
                                builder.dismiss()
                            }
                        }
                    }
                }
            }
        }

        if (item.itemId == 1) {
            val position = item.groupId
            AlertDialog.Builder(context)
                .setTitle("Are you sure you want to delete")
                .setNegativeButton("Cancel"){dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Ok"){dialog, _ ->
                    dialog.dismiss()
                    progressBar.visibility = ProgressBar.VISIBLE
                    databaseRef.child(keys!![position]).removeValue()
                        .addOnSuccessListener{
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(context,"Deleted successfully",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = ProgressBar.GONE
                            Toast.makeText(context,"Failed to delete, try again",Toast.LENGTH_SHORT).show()
                        }
                }
                .create().show()
        }
        return super.onContextItemSelected(item)
    }
}