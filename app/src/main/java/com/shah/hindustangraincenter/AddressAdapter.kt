package com.shah.hindustangraincenter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class AddressAdapter(private val adds: List<Address>,
                     private val check: Checks): RecyclerView.Adapter<AddressViewHolder>() {

    interface Checks{
        fun changeCheck(position: Int) {
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mng_add_layout,parent,false)
        return AddressViewHolder(view)
    }

    override fun getItemCount(): Int {
        return adds.size
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val add = adds[position]

        holder.name.text = add.name

        if (add.land.isNullOrEmpty()) {
            val address =
                "${add.addr1}, ${add.addr2}, ${add.city},\n ${add.state} - ${add.pin}"
            holder.addr.text = address
        } else {
            val address =
                "${add.addr1}, ${add.addr2}, ${add.land},\n ${add.city}, ${add.state} - ${add.pin}"
            holder.addr.text = address
        }

        holder.rd.isChecked = add.check!!
        if (add.check!!){
            check.changeCheck(position)
        }

        holder.rd.setOnClickListener {
            check.changeCheck(position)
        }
    }
}