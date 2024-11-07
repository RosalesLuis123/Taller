package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GestionarZonasActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addZoneButton: Button

    // Simulación de lista de zonas
    private val zonesList = mutableListOf("Zona 1", "Zona 2", "Zona 3", "Zona 4")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_zonas)

        // Inicializar RecyclerView y su adaptador
        recyclerView = findViewById(R.id.recycler_view_zones)
        addZoneButton = findViewById(R.id.add_zone_button)

        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val adapter = ZoneAdapter(zonesList)
        recyclerView.adapter = adapter

        // Acción del botón para agregar una nueva zona
        addZoneButton.setOnClickListener {
            // Lógica para agregar una nueva zona
            val intent = Intent(this, AddZoneActivity::class.java)
            startActivity(intent)
        }
    }
}
