package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.Zona

class GestionarZonasActivity : AppCompatActivity() {

    private lateinit var btnAgregarZona: Button
    private lateinit var listViewZonas: ListView

    companion object {
        val zonas = mutableListOf<Zona>()
        const val EDIT_ZONA_REQUEST_CODE = 1
        var zonaToEdit: Zona? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_zonas)

        btnAgregarZona = findViewById(R.id.btnAgregarZona)
        listViewZonas = findViewById(R.id.listViewZonas)

        // Configurar botón para agregar zona
        btnAgregarZona.setOnClickListener {
            val intent = Intent(this, AgregarZonaActivity::class.java)
            startActivity(intent)
        }

        // Configurar lista para editar zona
        listViewZonas.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            zonaToEdit = zonas[position]
            val intent = Intent(this, AgregarZonaActivity::class.java)
            startActivityForResult(intent, EDIT_ZONA_REQUEST_CODE)
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarListaZonas()
    }

    private fun actualizarListaZonas() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            zonas.map { "${it.nombre} - ${it.nombreUbicacion} - Rango: ${it.rango} km" }
        )
        listViewZonas.adapter = adapter
    }
}
