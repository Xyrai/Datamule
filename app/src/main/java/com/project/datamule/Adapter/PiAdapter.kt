package com.project.datamule.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.datamule.DataClass.Pi
import com.project.datamule.R
import kotlinx.android.synthetic.main.item_pi.view.*

class PiAdapter (private val pi_s: List<Pi>, val clickListener: (Pi) -> Unit) : RecyclerView.Adapter<PiAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_pi, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return pi_s.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(pi_s[position], clickListener)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        fun bind(pi: Pi, clickListener: (Pi) -> Unit) {
            itemView.tvName.text = pi.name
            itemView.setOnClickListener { clickListener(pi) }
        }
    }
}