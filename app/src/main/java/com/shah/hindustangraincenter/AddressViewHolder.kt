package com.shah.hindustangraincenter

import android.view.ContextMenu
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AddressViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
    val addr = itemView.findViewById(R.id.address) as TextView
    val name = itemView.findViewById(R.id.name) as TextView
    val rd = itemView.findViewById(R.id.a_rd) as RadioButton

    val  x = itemView.setOnCreateContextMenuListener(this)

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu?.add(this.adapterPosition,0,0,"Edit")
        menu?.add(this.adapterPosition,1,1,"Delete")
    }


}