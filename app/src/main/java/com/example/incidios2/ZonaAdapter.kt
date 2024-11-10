package com.example.incidios2

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.ArrayAdapter
import com.example.app.Zona

class ZonaAdapter(context: Context, private val zonas: MutableList<Zona>) :
    ArrayAdapter<Zona>(context, 0, zonas) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_zona, parent, false)
        val zona = zonas[position]

        val tvZonaInfo = view.findViewById<TextView>(R.id.tvZonaInfo)
        val btnEditarZona = view.findViewById<ImageButton>(R.id.btnEditarZona)
        val btnEliminarZona = view.findViewById<ImageButton>(R.id.btnEliminarZona)

        tvZonaInfo.text = "${zona.nombre} - ${zona.nombreUbicacion} - Rango: ${zona.rango} km"

        // Configurar botón de eliminar zona
        btnEliminarZona.setOnClickListener {
            zonas.removeAt(position)
            notifyDataSetChanged()
        }

        // Configurar botón de editar zona
        btnEditarZona.setOnClickListener {
            val intent = Intent(context, AgregarZonaActivity::class.java)
            GestionarZonasActivity.zonaToEdit = zona
            context.startActivity(intent)
        }

        return view
    }
}
