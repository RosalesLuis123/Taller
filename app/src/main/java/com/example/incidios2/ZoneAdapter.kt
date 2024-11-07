package com.example.incidios2  // Asegúrate de que esto esté en el paquete correcto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ZoneAdapter(private val zonesList: List<String>) : RecyclerView.Adapter<ZoneAdapter.ZoneViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ZoneViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.zoneText.text = zonesList[position]
    }

    override fun getItemCount(): Int {
        return zonesList.size
    }

    class ZoneViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val zoneText: TextView = itemView.findViewById(android.R.id.text1)
    }
}
