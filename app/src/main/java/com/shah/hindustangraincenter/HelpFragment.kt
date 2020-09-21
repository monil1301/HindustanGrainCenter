package com.shah.hindustangraincenter

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class HelpFragment: Fragment() {

    private lateinit var whatsppButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_help,container,false)
        whatsppButton = root.findViewById(R.id.wa)

        whatsppButton.setOnClickListener {
            val url = "https://api.whatsapp.com/send?phone=+919409248293"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        return root
    }
}