package com.example.incidios2
import android.location.Geocoder
import android.os.Bundle
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
import java.util.Locale

class AgregarZonaActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var tvRangoSeleccionado: TextView
    private lateinit var seekBarRango: SeekBar
    private lateinit var btnGuardarZona: Button
    private var googleMap: GoogleMap? = null
    private var selectedLocation: LatLng? = null
    private var nombreUbicacion: String? = null

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

        // Configurar el botón para guardar la zona
        btnGuardarZona.setOnClickListener {
            guardarZona()
        }
        GestionarZonasActivity.zonaToEdit?.let {
            selectedLocation = it.coordenadas
            nombreUbicacion = it.nombreUbicacion
            seekBarRango.progress = it.rango - 10
            agregarMarcadorZona(it.coordenadas)
            actualizarCirculo(it.rango.toDouble())
        }

// Actualizar la zona en la lista al guardar
        btnGuardarZona.setOnClickListener {
            if (GestionarZonasActivity.zonaToEdit != null) {
                GestionarZonasActivity.zonaToEdit?.apply {
                    coordenadas = selectedLocation ?: coordenadas
                    rango = seekBarRango.progress + 10
                    nombreUbicacion = this@AgregarZonaActivity.nombreUbicacion
                }
                GestionarZonasActivity.zonaToEdit = null
            } else {
                guardarZona()
            }
            finish()
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
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    nombreUbicacion = addresses[0].getAddressLine(0)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun guardarZona() {
        val rango = seekBarRango.progress + 10
        val location = selectedLocation
        if (location != null) {
            val zona = Zona(
                nombre = "Zona ${GestionarZonasActivity.zonas.size + 1}",
                coordenadas = location,
                rango = rango,
                nombreUbicacion = nombreUbicacion
            )
            GestionarZonasActivity.zonas.add(zona)
            finish()
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
