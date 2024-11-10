package com.example.incidios2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.Zona
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint

class GestionarZonasActivity : AppCompatActivity() {

    private lateinit var btnAgregarZona: Button
    private lateinit var listViewZonas: ListView
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    companion object {
        val zonas = mutableListOf<Zona>()
        var zonaToEdit: Zona? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_zonas)

        btnAgregarZona = findViewById(R.id.btnAgregarZona)
        listViewZonas = findViewById(R.id.listViewZonas)

        // Configurar botón para agregar nueva zona
        btnAgregarZona.setOnClickListener {
            zonaToEdit = null // Limpiar la zona a editar para crear una nueva
            val intent = Intent(this, AgregarZonaActivity::class.java)
            startActivity(intent)
        }

        // Cargar zonas del usuario actual
        cargarZonas()
    }

    private fun cargarZonas() {
        currentUser?.let { user ->
            firestore.collection("users").document(user.uid).collection("zonas")
                .get()
                .addOnSuccessListener { documents ->
                    zonas.clear()
                    for (document in documents) {
                        val nombre = document.getString("nombre") ?: ""
                        val coordenadas = document.getGeoPoint("coordenadas") ?: GeoPoint(0.0, 0.0)
                        val rango = document.getLong("rango")?.toInt() ?: 0
                        val nombreUbicacion = document.getString("nombreUbicacion") ?: ""
                        val zona = Zona(
                            nombre = nombre,
                            coordenadas = LatLng(coordenadas.latitude, coordenadas.longitude),
                            rango = rango,
                            nombreUbicacion = nombreUbicacion
                        )
                        zonas.add(zona)
                    }
                    actualizarListaZonas()
                }
                .addOnFailureListener { e ->
                    Log.w("GestionarZonasActivity", "Error al cargar las zonas", e)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarListaZonas()
    }

    private fun actualizarListaZonas() {
        val adapter = ZonaAdapter(this, zonas)
        listViewZonas.adapter = adapter
    }
}
