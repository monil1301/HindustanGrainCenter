package com.shah.hindustangraincenter

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlin.concurrent.timer

class PodFragment: Fragment() {

    private lateinit var pay: TextView
    private lateinit var time: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_pod,container,false)
        pay = root.findViewById(R.id.pay)
        time = root.findViewById(R.id.timer)

        val p = arguments?.get("price")

        pay.text = context?.getString(R.string.pricing,p)

        val timer = object : CountDownTimer(5000,1000){
            override fun onFinish() {
                (activity as MainActivity).goto(7)
                Toast.makeText(context,"Order Placed.....",Toast.LENGTH_SHORT).show()
            }

            override fun onTick(millisUntilFinished: Long) {
                val t = millisUntilFinished/1000
                time.text = getString(R.string.timer, t.toString())
            }
        }

        timer.start()
        return root
    }
}