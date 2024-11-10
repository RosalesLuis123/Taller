package com.example.incidios2

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.app.Zona
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Locale

class AgregarZonaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var tvRangoSeleccionado: TextView
    private lateinit var seekBarRango: SeekBar
    private lateinit var btnGuardarZona: Button
    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private var nombreUbicacion: String? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_zona)

        // Inicializar vistas
        mapView = findViewById(R.id.mapView)
        tvRangoSeleccionado = findViewById(R.id.tvRangoSeleccionado)
        seekBarRango = findViewById(R.id.seekBarRango)
        btnGuardarZona = findViewById(R.id.btnGuardarZona)

        // Configurar MapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Configurar SeekBar para el rango
        seekBarRango.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val rango = progress + 10
                tvRangoSeleccionado.text = "Rango: $rango km"
                actualizarCirculo(rango.toDouble())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Cargar datos de zona si se está editando
        GestionarZonasActivity.zonaToEdit?.let {
            selectedLocation = it.coordenadas
            nombreUbicacion = it.nombreUbicacion
            seekBarRango.progress = it.rango - 10
            agregarMarcadorZona(it.coordenadas)
            actualizarCirculo(it.rango.toDouble())
        }

        // Configurar el botón para guardar o actualizar la zona
        btnGuardarZona.setOnClickListener {
            if (GestionarZonasActivity.zonaToEdit != null) {
                // Editar zona existente
                actualizarZona()
            } else {
                // Agregar nueva zona
                guardarZona()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val bolivia = LatLng(-16.2902, -63.5887)
        googleMap?.apply {
            moveCamera(CameraUpdateFactory.newLatLngZoom(bolivia, 5.0f))
            setOnMapClickListener { latLng ->
                agregarMarcadorZona(latLng)
                selectedLocation = latLng
                actualizarCirculo(seekBarRango.progress + 10.0)
                obtenerNombreUbicacion(latLng)
            }
        }
    }

    private fun agregarMarcadorZona(latLng: LatLng) {
        googleMap?.apply {
            clear()
            addMarker(MarkerOptions().position(latLng).title("Zona Seleccionada"))
        }
    }

    private fun actualizarCirculo(radio: Double) {
        selectedLocation?.let {
            googleMap?.apply {
                clear()
                addMarker(MarkerOptions().position(it).title("Zona Seleccionada"))
                addCircle(
                    CircleOptions()
                        .center(it)
                        .radius(radio * 1000) // Convertir km a metros
                        .strokeColor(0x550000FF)
                        .fillColor(0x220000FF)
                )
            }
        }
    }

    private fun obtenerNombreUbicacion(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                nombreUbicacion = addresses[0].getAddressLine(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun guardarZona() {
        val rango = seekBarRango.progress + 10
        val location = selectedLocation
        if (location != null && currentUser != null) {
            val zona = Zona(
                nombre = "Zona ${GestionarZonasActivity.zonas.size + 1}",
                coordenadas = location,
                rango = rango,
                nombreUbicacion = nombreUbicacion
            )

            // Guardar en Firestore
            val zonaData = mapOf(
                "nombre" to zona.nombre,
                "coordenadas" to GeoPoint(location.latitude, location.longitude),
                "rango" to rango,
                "nombreUbicacion" to nombreUbicacion
            )
            firestore.collection("users").document(currentUser.uid).collection("zonas")
                .add(zonaData)
                .addOnSuccessListener {
                    Log.d("AgregarZonaActivity", "Zona guardada con éxito en Firestore")
                    GestionarZonasActivity.zonas.add(zona)
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.w("AgregarZonaActivity", "Error al guardar la zona", e)
                }
        }
    }

    private fun actualizarZona() {
        val rango = seekBarRango.progress + 10
        val location = selectedLocation
        val zonaToEdit = GestionarZonasActivity.zonaToEdit
        if (location != null && currentUser != null && zonaToEdit != null) {
            val zonaData = mapOf(
                "nombre" to zonaToEdit.nombre,
                "coordenadas" to GeoPoint(location.latitude, location.longitude),
                "rango" to rango,
                "nombreUbicacion" to nombreUbicacion
            )

            // Actualizar en Firestore
            firestore.collection("users").document(currentUser.uid).collection("zonas")
                .whereEqualTo("nombre", zonaToEdit.nombre) // Busca el documento por nombre
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        firestore.collection("users").document(currentUser.uid)
                            .collection("zonas").document(document.id).set(zonaData)
                            .addOnSuccessListener {
                                Log.d("AgregarZonaActivity", "Zona actualizada con éxito en Firestore")
                                zonaToEdit.coordenadas = location
                                zonaToEdit.rango = rango
                                zonaToEdit.nombreUbicacion = nombreUbicacion
                                GestionarZonasActivity.zonaToEdit = null
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.w("AgregarZonaActivity", "Error al actualizar la zona", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("AgregarZonaActivity", "Error al buscar la zona para actualizar", e)
                }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
